package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class HtmlUtils {

	public static String extractText(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(reader);
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		String textOnly = Jsoup.parse(sb.toString()).text();
		return textOnly;
	}

	public final static void main(String[] args) throws Exception {
		String site = "http://paradroid.automac.se/u4/history/history.html";
		File file = new File("book.txt");
		file.delete();
		
		//getLinks(site);
		
		URL url = new URL(site);

		FileWriter fw = new FileWriter("book.txt", true);
		fw.write(HtmlUtils.htmlToText(url.openStream()));
		fw.close();

	}

	public static List<String> extractLinks(String url) throws IOException {
		final ArrayList<String> result = new ArrayList<String>();

		Document doc = Jsoup.connect(url).get();

		Elements links = doc.select("a[href]");
		Elements media = doc.select("[src]");
		Elements imports = doc.select("link[href]");

		// href ...
		for (Element link : links) {
			result.add(link.attr("abs:href"));
		}

		// img ...
		for (Element src : media) {
			result.add(src.attr("abs:src"));
		}

		// js, css, ...
		for (Element link : imports) {
			result.add(link.attr("abs:href"));
		}
		return result;
	}

	public final static void getLinks(String site) throws Exception {
		List<String> links = HtmlUtils.extractLinks(site);

		for (String link : links) {
			System.out.println(link);

			if (link.startsWith("http://www.wordinfo.info/words/index/info/view_unit/4")) {

				URL url = new URL(link);
				URLConnection conn = url.openConnection();

				FileWriter fw = new FileWriter("book.txt", true);
				fw.write("\n\n-----------------------\n\n" + HtmlUtils.htmlToText(url.openStream()));
				fw.close();

			}

		}
	}

	public static String htmlToText(InputStream html) throws IOException {
		Document document = Jsoup.parse(html, null, "");
		Element body = document.body();

		return buildStringFromNode(body).toString();
	}

	public static StringBuffer buildStringFromNode(Node node) {
		StringBuffer buffer = new StringBuffer();

		if (node instanceof TextNode) {
			TextNode textNode = (TextNode) node;
			buffer.append(textNode.text().trim());
		}

		for (Node childNode : node.childNodes()) {
			buffer.append(buildStringFromNode(childNode));
		}

		if (node instanceof Element) {
			Element element = (Element) node;
			String tagName = element.tagName();
			if ("p".equals(tagName) || "br".equals(tagName)) {
				buffer.append("\n");
			}
			if ("h1".equals(tagName) || "h2".equals(tagName) || "h3".equals(tagName) || "h4".equals(tagName) || "dt".equals(tagName) || "dl".equals(tagName)) {
				buffer.append("\n\n");
			}
		}

		return buffer;
	}

}