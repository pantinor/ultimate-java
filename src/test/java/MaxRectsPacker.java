

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.imageio.ImageIO;

import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class MaxRectsPacker {
	private FreeRectChoiceHeuristic[] methods = FreeRectChoiceHeuristic.values();
	private MaxRects maxRects = new MaxRects();
	Settings settings;

	static public final float PI = 3.1415927f;
	static public final float radiansToDegrees = 180f / PI;
	static public final float radDeg = radiansToDegrees;
	static public final float degreesToRadians = PI / 180;
	static public final float degRad = degreesToRadians;
	
	public int alpha;

	public MaxRectsPacker(Settings settings) {
		this.settings = settings;
		if (settings.minWidth > settings.maxWidth)
			throw new RuntimeException("Page min width cannot be higher than max width.");
		if (settings.minHeight > settings.maxHeight)
			throw new RuntimeException("Page min height cannot be higher than max height.");
	}

	public MaxRectsPacker() {
		Settings settings = new Settings();
		settings.maxWidth = 160;
		settings.maxHeight = 320;
		settings.paddingX = 0;
		settings.paddingY = 0;
		settings.fast = false;
		
		this.settings = settings;
		
		if (settings.minWidth > settings.maxWidth)
			throw new RuntimeException("Page min width cannot be higher than max width.");
		if (settings.minHeight > settings.maxHeight)
			throw new RuntimeException("Page min height cannot be higher than max height.");
	}

	public ArrayList<Page> pack(ArrayList<Rect> inputRects) {
		for (int i = 0, nn = inputRects.size(); i < nn; i++) {
			Rect rect = inputRects.get(i);
			rect.width += settings.paddingX;
			rect.height += settings.paddingY;
		}

		if (settings.fast) {
			if (settings.rotation) {
				// Sort by longest side if rotation is enabled.
				Collections.sort(inputRects, new Comparator<Rect>() {
					public int compare(Rect o1, Rect o2) {
						int n1 = o1.width > o1.height ? o1.width : o1.height;
						int n2 = o2.width > o2.height ? o2.width : o2.height;
						return n2 - n1;
					}
				});
			} else {
				// Sort only by width (largest to smallest) if rotation is
				// disabled.
				Collections.sort(inputRects, new Comparator<Rect>() {
					public int compare(Rect o1, Rect o2) {
						return o2.width - o1.width;
					}
				});
			}
		}

		ArrayList<Page> pages = new ArrayList<Page>();
		while (inputRects.size() > 0) {
			Page result = packPage(inputRects);
			pages.add(result);
			inputRects = result.remainingRects;
		}
		return pages;
	}

	private Page packPage(ArrayList<Rect> inputRects) {
		int edgePaddingX = 0, edgePaddingY = 0;
		if (!settings.duplicatePadding) { // if duplicatePadding, edges get only
											// half padding.
			edgePaddingX = settings.paddingX;
			edgePaddingY = settings.paddingY;
		}

		// Find min size.
		int minWidth = Integer.MAX_VALUE;
		int minHeight = Integer.MAX_VALUE;
		for (int i = 0, nn = inputRects.size(); i < nn; i++) {
			Rect rect = inputRects.get(i);
			minWidth = Math.min(minWidth, rect.width);
			minHeight = Math.min(minHeight, rect.height);
			if (rect.width > settings.maxWidth && (!settings.rotation || rect.height > settings.maxWidth))
				throw new RuntimeException("Image does not fit with max page width " + settings.maxWidth + " and paddingX " + settings.paddingX + ": " + rect);
			if (rect.height > settings.maxHeight && (!settings.rotation || rect.width > settings.maxHeight))
				throw new RuntimeException("Image does not fit in max page height " + settings.maxHeight + " and paddingY " + settings.paddingY + ": " + rect);
		}
		minWidth = Math.max(minWidth, settings.minWidth);
		minHeight = Math.max(minHeight, settings.minHeight);

		System.out.print("Packing");

		// Find the minimal page size that fits all rects.
		BinarySearch widthSearch = new BinarySearch(minWidth, settings.maxWidth, settings.fast ? 25 : 15, settings.pot);
		BinarySearch heightSearch = new BinarySearch(minHeight, settings.maxHeight, settings.fast ? 25 : 15, settings.pot);
		int width = widthSearch.reset(), height = heightSearch.reset(), i = 0;
		Page bestResult = null;
		while (true) {
			Page bestWidthResult = null;
			while (width != -1) {
				Page result = packAtSize(true, width - edgePaddingX, height - edgePaddingY, inputRects);
				if (++i % 70 == 0)
					System.out.println();
				System.out.print(".");
				bestWidthResult = getBest(bestWidthResult, result);
				width = widthSearch.next(result == null);
			}
			bestResult = getBest(bestResult, bestWidthResult);
			height = heightSearch.next(bestWidthResult == null);
			if (height == -1)
				break;
			width = widthSearch.reset();
		}
		System.out.println();

		// Rects don't fit on one page. Fill a whole page and return.
		if (bestResult == null)
			bestResult = packAtSize(false, settings.maxWidth - edgePaddingX, settings.maxHeight - edgePaddingY, inputRects);

		return bestResult;
	}

	/**
	 * @param fully
	 *            If true, the only results that pack all rects will be
	 *            considered. If false, all results are considered, not all
	 *            rects may be packed.
	 */
	private Page packAtSize(boolean fully, int width, int height, ArrayList<Rect> inputRects) {
		Page bestResult = null;
		for (int i = 0, n = methods.length; i < n; i++) {
			maxRects.init(width, height);
			Page result;
			if (!settings.fast) {
				result = maxRects.pack(inputRects, methods[i]);
			} else {
				ArrayList<Rect> remaining = new ArrayList<Rect>();
				for (int ii = 0, nn = inputRects.size(); ii < nn; ii++) {
					Rect rect = inputRects.get(ii);
					if (maxRects.insert(rect, methods[i]) == null) {
						while (ii < nn)
							remaining.add(inputRects.get(ii++));
					}
				}
				result = maxRects.getResult();
				result.remainingRects = remaining;
			}
			if (fully && result.remainingRects.size() > 0)
				continue;
			if (result.outputRects.size() == 0)
				continue;
			bestResult = getBest(bestResult, result);
		}
		return bestResult;
	}

	private Page getBest(Page result1, Page result2) {
		if (result1 == null)
			return result2;
		if (result2 == null)
			return result1;
		return result1.occupancy > result2.occupancy ? result1 : result2;
	}

	static class BinarySearch {
		int min, max, fuzziness, low, high, current;
		boolean pot;

		public BinarySearch(int min, int max, int fuzziness, boolean pot) {
			this.pot = pot;
			this.fuzziness = pot ? 0 : fuzziness;
			this.min = pot ? (int) (Math.log(nextPowerOfTwo(min)) / Math.log(2)) : min;
			this.max = pot ? (int) (Math.log(nextPowerOfTwo(max)) / Math.log(2)) : max;
		}

		public int reset() {
			low = min;
			high = max;
			current = (low + high) >>> 1;
			return pot ? (int) Math.pow(2, current) : current;
		}

		public int next(boolean result) {
			if (low >= high)
				return -1;
			if (result)
				low = current + 1;
			else
				high = current - 1;
			current = (low + high) >>> 1;
			if (Math.abs(low - high) < fuzziness)
				return -1;
			return pot ? (int) Math.pow(2, current) : current;
		}
	}

	class MaxRects {
		private int binWidth;
		private int binHeight;
		private final ArrayList<Rect> usedRectangles = new ArrayList<Rect>();
		private final ArrayList<Rect> freeRectangles = new ArrayList<Rect>();

		public void init(int width, int height) {
			binWidth = width;
			binHeight = height;

			usedRectangles.clear();
			freeRectangles.clear();
			Rect n = new Rect();
			n.x = 0;
			n.y = 0;
			n.width = width;
			n.height = height;
			freeRectangles.add(n);
		}

		/** Packs a single image. Order is defined externally. */
		public Rect insert(Rect rect, FreeRectChoiceHeuristic method) {
			Rect newNode = ScoreRect(rect, method);
			if (newNode.height == 0)
				return null;

			int numRectanglesToProcess = freeRectangles.size();
			for (int i = 0; i < numRectanglesToProcess; ++i) {
				if (SplitFreeNode(freeRectangles.get(i), newNode)) {
					freeRectangles.remove(i);
					--i;
					--numRectanglesToProcess;
				}
			}

			PruneFreeList();

			Rect bestNode = new Rect();
			bestNode.set(rect);
			bestNode.score1 = newNode.score1;
			bestNode.score2 = newNode.score2;
			bestNode.x = newNode.x;
			bestNode.y = newNode.y;
			bestNode.width = newNode.width;
			bestNode.height = newNode.height;
			bestNode.rotated = newNode.rotated;

			usedRectangles.add(bestNode);
			return bestNode;
		}

		/**
		 * For each rectangle, packs each one then chooses the best and packs
		 * that. Slow!
		 */
		public Page pack(ArrayList<Rect> rects, FreeRectChoiceHeuristic method) {
			rects = new ArrayList<Rect>(rects);
			while (rects.size() > 0) {
				int bestRectIndex = -1;
				Rect bestNode = new Rect();
				bestNode.score1 = Integer.MAX_VALUE;
				bestNode.score2 = Integer.MAX_VALUE;

				// Find the next rectangle that packs best.
				for (int i = 0; i < rects.size(); i++) {
					Rect newNode = ScoreRect(rects.get(i), method);
					if (newNode.score1 < bestNode.score1 || (newNode.score1 == bestNode.score1 && newNode.score2 < bestNode.score2)) {
						bestNode.set(rects.get(i));
						bestNode.score1 = newNode.score1;
						bestNode.score2 = newNode.score2;
						bestNode.x = newNode.x;
						bestNode.y = newNode.y;
						bestNode.width = newNode.width;
						bestNode.height = newNode.height;
						bestNode.rotated = newNode.rotated;
						bestRectIndex = i;
					}
				}

				if (bestRectIndex == -1)
					break;

				PlaceRect(bestNode);
				rects.remove(bestRectIndex);
			}

			Page result = getResult();
			result.remainingRects = rects;
			return result;
		}

		public Page getResult() {
			int w = 0, h = 0;
			for (int i = 0; i < usedRectangles.size(); i++) {
				Rect rect = usedRectangles.get(i);
				w = Math.max(w, rect.x + rect.width);
				h = Math.max(h, rect.y + rect.height);
			}
			Page result = new Page();
			result.outputRects = new ArrayList<Rect>(usedRectangles);
			result.occupancy = getOccupancy();
			result.width = w;
			result.height = h;
			return result;
		}

		private void PlaceRect(Rect node) {
			int numRectanglesToProcess = freeRectangles.size();
			for (int i = 0; i < numRectanglesToProcess; i++) {
				if (SplitFreeNode(freeRectangles.get(i), node)) {
					freeRectangles.remove(i);
					--i;
					--numRectanglesToProcess;
				}
			}

			PruneFreeList();

			usedRectangles.add(node);
		}

		private Rect ScoreRect(Rect rect, FreeRectChoiceHeuristic method) {
			int width = rect.width;
			int height = rect.height;
			int rotatedWidth = height - settings.paddingY + settings.paddingX;
			int rotatedHeight = width - settings.paddingX + settings.paddingY;
			boolean rotate = rect.canRotate && settings.rotation;

			Rect newNode = null;
			switch (method) {
			case BestShortSideFit:
				newNode = FindPositionForNewNodeBestShortSideFit(width, height, rotatedWidth, rotatedHeight, rotate);
				break;
			case BottomLeftRule:
				newNode = FindPositionForNewNodeBottomLeft(width, height, rotatedWidth, rotatedHeight, rotate);
				break;
			case ContactPointRule:
				newNode = FindPositionForNewNodeContactPoint(width, height, rotatedWidth, rotatedHeight, rotate);
				newNode.score1 = -newNode.score1; // Reverse since we are
													// minimizing, but for
													// contact point score
													// bigger is better.
				break;
			case BestLongSideFit:
				newNode = FindPositionForNewNodeBestLongSideFit(width, height, rotatedWidth, rotatedHeight, rotate);
				break;
			case BestAreaFit:
				newNode = FindPositionForNewNodeBestAreaFit(width, height, rotatedWidth, rotatedHeight, rotate);
				break;
			}

			// Cannot fit the current rectangle.
			if (newNode.height == 0) {
				newNode.score1 = Integer.MAX_VALUE;
				newNode.score2 = Integer.MAX_VALUE;
			}

			return newNode;
		}

		// / Computes the ratio of used surface area.
		private float getOccupancy() {
			int usedSurfaceArea = 0;
			for (int i = 0; i < usedRectangles.size(); i++)
				usedSurfaceArea += usedRectangles.get(i).width * usedRectangles.get(i).height;
			return (float) usedSurfaceArea / (binWidth * binHeight);
		}

		private Rect FindPositionForNewNodeBottomLeft(int width, int height, int rotatedWidth, int rotatedHeight, boolean rotate) {
			Rect bestNode = new Rect();

			bestNode.score1 = Integer.MAX_VALUE; // best y, score2 is best x

			for (int i = 0; i < freeRectangles.size(); i++) {
				// Try to place the rectangle in upright (non-rotated)
				// orientation.
				if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
					int topSideY = freeRectangles.get(i).y + height;
					if (topSideY < bestNode.score1 || (topSideY == bestNode.score1 && freeRectangles.get(i).x < bestNode.score2)) {
						bestNode.x = freeRectangles.get(i).x;
						bestNode.y = freeRectangles.get(i).y;
						bestNode.width = width;
						bestNode.height = height;
						bestNode.score1 = topSideY;
						bestNode.score2 = freeRectangles.get(i).x;
						bestNode.rotated = false;
					}
				}
				if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
					int topSideY = freeRectangles.get(i).y + rotatedHeight;
					if (topSideY < bestNode.score1 || (topSideY == bestNode.score1 && freeRectangles.get(i).x < bestNode.score2)) {
						bestNode.x = freeRectangles.get(i).x;
						bestNode.y = freeRectangles.get(i).y;
						bestNode.width = rotatedWidth;
						bestNode.height = rotatedHeight;
						bestNode.score1 = topSideY;
						bestNode.score2 = freeRectangles.get(i).x;
						bestNode.rotated = true;
					}
				}
			}
			return bestNode;
		}

		private Rect FindPositionForNewNodeBestShortSideFit(int width, int height, int rotatedWidth, int rotatedHeight, boolean rotate) {
			Rect bestNode = new Rect();
			bestNode.score1 = Integer.MAX_VALUE;

			for (int i = 0; i < freeRectangles.size(); i++) {
				// Try to place the rectangle in upright (non-rotated)
				// orientation.
				if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
					int leftoverHoriz = Math.abs(freeRectangles.get(i).width - width);
					int leftoverVert = Math.abs(freeRectangles.get(i).height - height);
					int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
					int longSideFit = Math.max(leftoverHoriz, leftoverVert);

					if (shortSideFit < bestNode.score1 || (shortSideFit == bestNode.score1 && longSideFit < bestNode.score2)) {
						bestNode.x = freeRectangles.get(i).x;
						bestNode.y = freeRectangles.get(i).y;
						bestNode.width = width;
						bestNode.height = height;
						bestNode.score1 = shortSideFit;
						bestNode.score2 = longSideFit;
						bestNode.rotated = false;
					}
				}

				if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
					int flippedLeftoverHoriz = Math.abs(freeRectangles.get(i).width - rotatedWidth);
					int flippedLeftoverVert = Math.abs(freeRectangles.get(i).height - rotatedHeight);
					int flippedShortSideFit = Math.min(flippedLeftoverHoriz, flippedLeftoverVert);
					int flippedLongSideFit = Math.max(flippedLeftoverHoriz, flippedLeftoverVert);

					if (flippedShortSideFit < bestNode.score1 || (flippedShortSideFit == bestNode.score1 && flippedLongSideFit < bestNode.score2)) {
						bestNode.x = freeRectangles.get(i).x;
						bestNode.y = freeRectangles.get(i).y;
						bestNode.width = rotatedWidth;
						bestNode.height = rotatedHeight;
						bestNode.score1 = flippedShortSideFit;
						bestNode.score2 = flippedLongSideFit;
						bestNode.rotated = true;
					}
				}
			}

			return bestNode;
		}

		private Rect FindPositionForNewNodeBestLongSideFit(int width, int height, int rotatedWidth, int rotatedHeight, boolean rotate) {
			Rect bestNode = new Rect();

			bestNode.score2 = Integer.MAX_VALUE;

			for (int i = 0; i < freeRectangles.size(); i++) {
				// Try to place the rectangle in upright (non-rotated)
				// orientation.
				if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
					int leftoverHoriz = Math.abs(freeRectangles.get(i).width - width);
					int leftoverVert = Math.abs(freeRectangles.get(i).height - height);
					int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
					int longSideFit = Math.max(leftoverHoriz, leftoverVert);

					if (longSideFit < bestNode.score2 || (longSideFit == bestNode.score2 && shortSideFit < bestNode.score1)) {
						bestNode.x = freeRectangles.get(i).x;
						bestNode.y = freeRectangles.get(i).y;
						bestNode.width = width;
						bestNode.height = height;
						bestNode.score1 = shortSideFit;
						bestNode.score2 = longSideFit;
						bestNode.rotated = false;
					}
				}

				if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
					int leftoverHoriz = Math.abs(freeRectangles.get(i).width - rotatedWidth);
					int leftoverVert = Math.abs(freeRectangles.get(i).height - rotatedHeight);
					int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
					int longSideFit = Math.max(leftoverHoriz, leftoverVert);

					if (longSideFit < bestNode.score2 || (longSideFit == bestNode.score2 && shortSideFit < bestNode.score1)) {
						bestNode.x = freeRectangles.get(i).x;
						bestNode.y = freeRectangles.get(i).y;
						bestNode.width = rotatedWidth;
						bestNode.height = rotatedHeight;
						bestNode.score1 = shortSideFit;
						bestNode.score2 = longSideFit;
						bestNode.rotated = true;
					}
				}
			}
			return bestNode;
		}

		private Rect FindPositionForNewNodeBestAreaFit(int width, int height, int rotatedWidth, int rotatedHeight, boolean rotate) {
			Rect bestNode = new Rect();

			bestNode.score1 = Integer.MAX_VALUE; // best area fit, score2 is
													// best short side fit

			for (int i = 0; i < freeRectangles.size(); i++) {
				int areaFit = freeRectangles.get(i).width * freeRectangles.get(i).height - width * height;

				// Try to place the rectangle in upright (non-rotated)
				// orientation.
				if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
					int leftoverHoriz = Math.abs(freeRectangles.get(i).width - width);
					int leftoverVert = Math.abs(freeRectangles.get(i).height - height);
					int shortSideFit = Math.min(leftoverHoriz, leftoverVert);

					if (areaFit < bestNode.score1 || (areaFit == bestNode.score1 && shortSideFit < bestNode.score2)) {
						bestNode.x = freeRectangles.get(i).x;
						bestNode.y = freeRectangles.get(i).y;
						bestNode.width = width;
						bestNode.height = height;
						bestNode.score2 = shortSideFit;
						bestNode.score1 = areaFit;
						bestNode.rotated = false;
					}
				}

				if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
					int leftoverHoriz = Math.abs(freeRectangles.get(i).width - rotatedWidth);
					int leftoverVert = Math.abs(freeRectangles.get(i).height - rotatedHeight);
					int shortSideFit = Math.min(leftoverHoriz, leftoverVert);

					if (areaFit < bestNode.score1 || (areaFit == bestNode.score1 && shortSideFit < bestNode.score2)) {
						bestNode.x = freeRectangles.get(i).x;
						bestNode.y = freeRectangles.get(i).y;
						bestNode.width = rotatedWidth;
						bestNode.height = rotatedHeight;
						bestNode.score2 = shortSideFit;
						bestNode.score1 = areaFit;
						bestNode.rotated = true;
					}
				}
			}
			return bestNode;
		}

		// / Returns 0 if the two intervals i1 and i2 are disjoint, or the
		// length of their overlap otherwise.
		private int CommonIntervalLength(int i1start, int i1end, int i2start, int i2end) {
			if (i1end < i2start || i2end < i1start)
				return 0;
			return Math.min(i1end, i2end) - Math.max(i1start, i2start);
		}

		private int ContactPointScoreNode(int x, int y, int width, int height) {
			int score = 0;

			if (x == 0 || x + width == binWidth)
				score += height;
			if (y == 0 || y + height == binHeight)
				score += width;

			for (int i = 0; i < usedRectangles.size(); i++) {
				if (usedRectangles.get(i).x == x + width || usedRectangles.get(i).x + usedRectangles.get(i).width == x)
					score += CommonIntervalLength(usedRectangles.get(i).y, usedRectangles.get(i).y + usedRectangles.get(i).height, y, y + height);
				if (usedRectangles.get(i).y == y + height || usedRectangles.get(i).y + usedRectangles.get(i).height == y)
					score += CommonIntervalLength(usedRectangles.get(i).x, usedRectangles.get(i).x + usedRectangles.get(i).width, x, x + width);
			}
			return score;
		}

		private Rect FindPositionForNewNodeContactPoint(int width, int height, int rotatedWidth, int rotatedHeight, boolean rotate) {
			Rect bestNode = new Rect();

			bestNode.score1 = -1; // best contact score

			for (int i = 0; i < freeRectangles.size(); i++) {
				// Try to place the rectangle in upright (non-rotated)
				// orientation.
				if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
					int score = ContactPointScoreNode(freeRectangles.get(i).x, freeRectangles.get(i).y, width, height);
					if (score > bestNode.score1) {
						bestNode.x = freeRectangles.get(i).x;
						bestNode.y = freeRectangles.get(i).y;
						bestNode.width = width;
						bestNode.height = height;
						bestNode.score1 = score;
						bestNode.rotated = false;
					}
				}
				if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
					// This was width,height -- bug fixed?
					int score = ContactPointScoreNode(freeRectangles.get(i).x, freeRectangles.get(i).y, rotatedWidth, rotatedHeight);
					if (score > bestNode.score1) {
						bestNode.x = freeRectangles.get(i).x;
						bestNode.y = freeRectangles.get(i).y;
						bestNode.width = rotatedWidth;
						bestNode.height = rotatedHeight;
						bestNode.score1 = score;
						bestNode.rotated = true;
					}
				}
			}
			return bestNode;
		}

		private boolean SplitFreeNode(Rect freeNode, Rect usedNode) {
			// Test with SAT if the rectangles even intersect.
			if (usedNode.x >= freeNode.x + freeNode.width || usedNode.x + usedNode.width <= freeNode.x || usedNode.y >= freeNode.y + freeNode.height || usedNode.y + usedNode.height <= freeNode.y)
				return false;

			if (usedNode.x < freeNode.x + freeNode.width && usedNode.x + usedNode.width > freeNode.x) {
				// New node at the top side of the used node.
				if (usedNode.y > freeNode.y && usedNode.y < freeNode.y + freeNode.height) {
					Rect newNode = new Rect(freeNode);
					newNode.height = usedNode.y - newNode.y;
					freeRectangles.add(newNode);
				}

				// New node at the bottom side of the used node.
				if (usedNode.y + usedNode.height < freeNode.y + freeNode.height) {
					Rect newNode = new Rect(freeNode);
					newNode.y = usedNode.y + usedNode.height;
					newNode.height = freeNode.y + freeNode.height - (usedNode.y + usedNode.height);
					freeRectangles.add(newNode);
				}
			}

			if (usedNode.y < freeNode.y + freeNode.height && usedNode.y + usedNode.height > freeNode.y) {
				// New node at the left side of the used node.
				if (usedNode.x > freeNode.x && usedNode.x < freeNode.x + freeNode.width) {
					Rect newNode = new Rect(freeNode);
					newNode.width = usedNode.x - newNode.x;
					freeRectangles.add(newNode);
				}

				// New node at the right side of the used node.
				if (usedNode.x + usedNode.width < freeNode.x + freeNode.width) {
					Rect newNode = new Rect(freeNode);
					newNode.x = usedNode.x + usedNode.width;
					newNode.width = freeNode.x + freeNode.width - (usedNode.x + usedNode.width);
					freeRectangles.add(newNode);
				}
			}

			return true;
		}

		private void PruneFreeList() {
			// Go through each pair and remove any rectangle that is redundant.
			for (int i = 0; i < freeRectangles.size(); i++) {
				for (int j = i + 1; j < freeRectangles.size(); ++j) {
					if (IsContainedIn(freeRectangles.get(i), freeRectangles.get(j))) {
						freeRectangles.remove(i);
						--i;
						break;
					}
					if (IsContainedIn(freeRectangles.get(j), freeRectangles.get(i))) {
						freeRectangles.remove(j);
						--j;
					}
				}
			}
		}

		private boolean IsContainedIn(Rect a, Rect b) {
			return a.x >= b.x && a.y >= b.y && a.x + a.width <= b.x + b.width && a.y + a.height <= b.y + b.height;
		}
	}

	static public enum FreeRectChoiceHeuristic {
		// BSSF: Positions the rectangle against the short side of a free
		// rectangle into which it fits the best.
		BestShortSideFit,
		// BLSF: Positions the rectangle against the long side of a free
		// rectangle into which it fits the best.
		BestLongSideFit,
		// BAF: Positions the rectangle into the smallest free rect into which
		// it fits.
		BestAreaFit,
		// BL: Does the Tetris placement.
		BottomLeftRule,
		// CP: Choosest the placement where the rectangle touches other rects as
		// much as possible.
		ContactPointRule
	};

	static public int nextPowerOfTwo(int value) {
		if (value == 0)
			return 1;
		value--;
		value |= value >> 1;
		value |= value >> 2;
		value |= value >> 4;
		value |= value >> 8;
		value |= value >> 16;
		return value + 1;
	}

	static public class Page {
		public String imageName;
		public ArrayList<Rect> outputRects, remainingRects;
		public float occupancy;
		public int x, y, width, height;
	}

	static public class Rect {
		public String name;
		public BufferedImage image;
		public int offsetX, offsetY, originalWidth, originalHeight;
		public int x, y, width, height;
		public int index;
		public boolean rotated;
		public ArrayList<Rect> aliases = new ArrayList<Rect>();
		public int[] splits;
		public int[] pads;
		public boolean canRotate = true;
		public int tiledIndex;

		int score1, score2;

		Rect(BufferedImage source, int left, int top, int newWidth, int newHeight) {
			image = new BufferedImage(source.getColorModel(), source.getRaster().createWritableChild(left, top, newWidth, newHeight, 0, 0, null), source.getColorModel().isAlphaPremultiplied(), null);
			offsetX = left;
			offsetY = top;
			originalWidth = source.getWidth();
			originalHeight = source.getHeight();
			width = newWidth;
			height = newHeight;
		}
		
		Rect(int x, int y, int width, int height) {
			offsetX = 0;
			offsetY = 0;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			originalWidth = width;
			originalHeight = height;
		}

		Rect() {
		}

		Rect(Rect rect) {
			setSize(rect);
		}

		void setSize(Rect rect) {
			x = rect.x;
			y = rect.y;
			width = rect.width;
			height = rect.height;
		}

		void set(Rect rect) {
			name = rect.name;
			image = rect.image;
			offsetX = rect.offsetX;
			offsetY = rect.offsetY;
			originalWidth = rect.originalWidth;
			originalHeight = rect.originalHeight;
			x = rect.x;
			y = rect.y;
			width = rect.width;
			height = rect.height;
			index = rect.index;
			rotated = rect.rotated;
			aliases = rect.aliases;
			splits = rect.splits;
			pads = rect.pads;
			canRotate = rect.canRotate;
			score1 = rect.score1;
			score2 = rect.score2;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Rect other = (Rect) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return String.format("Rect [name=%s, x=%s, y=%s, width=%s, height=%s, index=%s]", name, x, y, width, height, index);
		}


	}


	public void writeImages(File outputDir, ArrayList<Page> pages, String packFileName) {
		String imageName = packFileName;
		int dotIndex = imageName.lastIndexOf('.');
		if (dotIndex != -1)
			imageName = imageName.substring(0, dotIndex);

		int fileIndex = 0;
		for (Page page : pages) {
			int width = page.width, height = page.height;
			int paddingX = settings.paddingX;
			int paddingY = settings.paddingY;
			if (settings.duplicatePadding) {
				paddingX /= 2;
				paddingY /= 2;
			}
			width -= settings.paddingX;
			height -= settings.paddingY;
			if (settings.edgePadding) {
				page.x = paddingX;
				page.y = paddingY;
				width += paddingX * 2;
				height += paddingY * 2;
			}
			if (settings.pot) {
				width = MaxRectsPacker.nextPowerOfTwo(width);
				height = MaxRectsPacker.nextPowerOfTwo(height);
			}
			width = Math.max(settings.minWidth, width);
			height = Math.max(settings.minHeight, height);


			File outputFile;
			while (true) {
				outputFile = new File(outputDir, imageName + (fileIndex++ == 0 ? "" : fileIndex) + ".png");
				if (!outputFile.exists())
					break;
			}
			page.imageName = outputFile.getName();

			//BufferedImage canvas = ImageTransparency.createTransparentImage(width, height);
			BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = (Graphics2D) canvas.getGraphics();

			System.out.println("Writing " + canvas.getWidth() + "x" + canvas.getHeight() + ": " + outputFile);

			for (Rect rect : page.outputRects) {
				
				int rectX = page.x + rect.x;
				int rectY = page.y + page.height - rect.y - rect.height;
				
				if (rect.rotated) {
					g.translate(rectX, rectY);
					g.rotate(-90 * degreesToRadians);
					g.translate(-rectX, -rectY);
					g.translate(-(rect.height - settings.paddingY), 0);
				}
				BufferedImage image = rect.image;
				if (settings.duplicatePadding) {
					int amountX = settings.paddingX / 2;
					int amountY = settings.paddingY / 2;
					int imageWidth = image.getWidth();
					int imageHeight = image.getHeight();
					// Copy corner pixels to fill corners of the padding.
					g.drawImage(image, rectX - amountX, rectY - amountY, rectX, rectY, 0, 0, 1, 1, null);
					g.drawImage(image, rectX + imageWidth, rectY - amountY, rectX + imageWidth + amountX, rectY, imageWidth - 1, 0, imageWidth, 1, null);
					g.drawImage(image, rectX - amountX, rectY + imageHeight, rectX, rectY + imageHeight + amountY, 0, imageHeight - 1, 1, imageHeight, null);
					g.drawImage(image, rectX + imageWidth, rectY + imageHeight, rectX + imageWidth + amountX, rectY + imageHeight + amountY, imageWidth - 1, imageHeight - 1, imageWidth, imageHeight, null);
					// Copy edge pixels into padding.
					g.drawImage(image, rectX, rectY - amountY, rectX + imageWidth, rectY, 0, 0, imageWidth, 1, null);
					g.drawImage(image, rectX, rectY + imageHeight, rectX + imageWidth, rectY + imageHeight + amountY, 0, imageHeight - 1, imageWidth, imageHeight, null);
					g.drawImage(image, rectX - amountX, rectY, rectX, rectY + imageHeight, 0, 0, 1, imageHeight, null);
					g.drawImage(image, rectX + imageWidth, rectY, rectX + imageWidth + amountX, rectY + imageHeight, imageWidth - 1, 0, imageWidth, imageHeight, null);
				}
				g.drawImage(image, rectX, rectY, null);
				if (rect.rotated) {
					g.translate(rect.height - settings.paddingY, 0);
					g.translate(rectX, rectY);
					g.rotate(90 * degreesToRadians);
					g.translate(-rectX, -rectY);
				}
				if (settings.debug) {
					g.setColor(Color.magenta);
					g.drawRect(rectX, rectY, rect.width - settings.paddingX - 1, rect.height - settings.paddingY - 1);
				}
			}

			if (settings.debug) {
				g.setColor(Color.magenta);
				g.drawRect(0, 0, width - 1, height - 1);
			}
			
			try {
				//BufferedImage img = ImageTransparency.convert(canvas, outputFile.getAbsolutePath(), alpha);
				//ImageIO.write(img, "PNG", outputFile);
				ImageIO.write(canvas, "PNG", outputFile);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public void writePackFile(File outputDir, ArrayList<Page> pages, String packFileName) throws IOException {
		File packFile = new File(outputDir, packFileName);
		if (packFile.exists()) packFile.delete();
		FileWriter writer = new FileWriter(packFile, true);
		for (Page page : pages) {
			writer.write("\n" + page.imageName + "\n");
			writer.write("format: RGBA8888\n");
			writer.write("filter: Linear,Linear\n");
			writer.write("repeat: none\n");

			for (Rect rect : page.outputRects) {
				writeRect(writer, page, rect);
				for (Rect alias : rect.aliases) {
					alias.setSize(rect);
					writeRect(writer, page, alias);
				}
			}
		}
		writer.close();
	}

	private void writeRect(FileWriter writer, Page page, Rect rect) throws IOException {
		String rectName = rect.name;
		writer.write(rectName + "\n");
		writer.write("  rotate: " + rect.rotated + "\n");
		writer.write("  xy: " + (page.x + rect.x) + ", " + (page.y + page.height - rect.height - rect.y) + "\n");
		writer.write("  size: " + rect.image.getWidth() + ", " + rect.image.getHeight() + "\n");
		if (rect.splits != null) {
			writer.write("  split: " + rect.splits[0] + ", " + rect.splits[1] + ", " + rect.splits[2] + ", " + rect.splits[3] + "\n");
		}
		if (rect.pads != null) {
			if (rect.splits == null)
				writer.write("  split: 0, 0, 0, 0\n");
			writer.write("  pad: " + rect.pads[0] + ", " + rect.pads[1] + ", " + rect.pads[2] + ", " + rect.pads[3] + "\n");
		}
		writer.write("  orig: " + rect.originalWidth + ", " + rect.originalHeight + "\n");
		writer.write("  offset: " + rect.offsetX + ", " + (rect.originalHeight - rect.image.getHeight() - rect.offsetY) + "\n");
		writer.write("  index: " + rect.index + "\n");
		
		//System.out.println("Writing: " + rectName);
		//ImageIO.write(rect.image, "PNG", new File(BiowareBamSpriteCreator.OUTPUTDIR+"test\\"+rectName+"-"+rect.index+".png"));
	}
	
	public void writePackFileWithRects(File outputDir, String packFileName, ArrayList<Rect> rects, String imageName) throws IOException {
		File packFile = new File(outputDir, packFileName);
		if (packFile.exists()) packFile.delete();

		FileWriter writer = new FileWriter(packFile, true);
		
		writer.write("\n" + imageName + "\n");
		writer.write("format: RGBA8888\n");
		writer.write("filter: Linear,Linear\n");
		writer.write("repeat: none\n");

		for (Rect rect : rects) {
			String rectName = rect.name;
			writer.write(rectName + "\n");
			writer.write("  rotate: false\n");
			writer.write("  xy: " + rect.x + ", " + rect.y + "\n");
			writer.write("  size: " + rect.width + ", " + rect.height + "\n");
			writer.write("  orig: " + rect.originalWidth + ", " + rect.originalHeight + "\n");
			writer.write("  offset: 0, 0\n");
			writer.write("  index: " + rect.index + "\n");
		}
		
		writer.close();
	}

}