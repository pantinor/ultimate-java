package ultima;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Intro implements Screen {
		
	public static String[] questions = new String[28];
	public static String[] script = new String[29];

	static {
		questions[0] = "Entrusted to deliver an uncounted purse of gold, thou Dost meet a poor beggar. \nDost thou A) deliver the gold knowing the Trust in thee was well placed, or B) show Compassion, giving the beggar a coin, knowing it won't be missed?";
		questions[1] = "Thou has been prohibited by thy absent Lord from joining thy friends in a close pitched battle. \nDost thou A) refrain, so thou may Honesty claim obedience, or B) show Valor, and aid thy comrades, knowing thou may deny it later?";
		questions[2] = "A merchant owes thy friend money, now long past due.  Thou \nDost see the same merchant drop a purse of gold. \nDost thou A) Honestly return the purse intact, or B) Justly give thy friend a portion of the gold first?";
		questions[3] = "Thee and thy friend are valiant but penniless warriors.  Thou both go out to slay a mighty dragon.  Thy friend thinks he slew it; thee did.  When asked, \nDost thou A) Truthfully claim the gold, or B) Allow thy friend the large reward?";
		questions[4] = "Thou art sworn to protect thy Lord at any cost, yet thou know he hath committed a crime.  Authorities ask thee of the affair.  \nDost thou A) break thine oath by Honestly speaking, or B) uphold Honor by silently keeping thine oath?";
		questions[5] = "Thy friend seeks admittance to thy Spiritual order. Thou art asked to vouch for his purity of Spirit, of which thou art unsure.  \nDost thou A) Honestly express thy doubt, or B) Vouch for him hoping for his Spiritual improvement?";
		questions[6] = "Thy Lord mistakenly believes he slew a dragon.  Thou hast proof that thy lance felled the beast.  When asked, \nDost thou A) Honestly claim the kill and the prize, or B) Humbly permit thy Lord his belief?";
		questions[7] = "Thou Dost manage to disarm thy mortal enemy in a duel.  He is at thy mercy. \nDost thou A) show Compassion by permitting him to yield, or B) slay him as expected of a Valiant duelist?";
		questions[8] = "After 20 years thou hast found the slayer of thy best friends.  The villain proves to be a man who provides the sole support for a young girl.  \nDost thou A) spare him in Compassion for the girl, or B) slay him in the name of Justice?";
		questions[9] = "Thee and thy friends have been routed and ordered to retreat.  In defiance of thy orders, \nDost thou A) stop in Compassion to aid a wounded companion, or B) Sacrifice thyself to slow the pursuing enemy, so others may escape?";
		questions[10] = "Thou art sworn to uphold a Lord who participates in the forbidden torture of prisoners.  Each night their cries of pain reach thee.  \nDost thou A) show Compassion by reporting the deeds, or B) Honor thy oath and ignore the deeds?";
		questions[11] = "Thou hast been taught to preserve all life as sacred. A man lies fatally stung by a venomous serpent.  He pleads for a merciful death.  \nDost thou A) show Compassion and end his pain, or B) heed thy Spiritual beliefs and refuse?";
		questions[12] = "As one of the King's Guard, thy Captain has asked that one amongst you visit a hospital to cheer the children with tales of thy valiant deeds.  \nDost thou A) Show thy Compassion and play the braggart, or B) Humbly let another go?";
		questions[13] = "Thou hast been sent to secure a needed treaty with a distant Lord.  Thy host is agreeable to the proposal but insults thy country at dinner.  \nDost thou A) Valiantly bear the slurs, or B) Justly rise and demand an apology?";
		questions[14] = "A mighty knight accosts thee and demands thy food.  \nDost thou A) Valiantly refuse and engage the knight, or B) Sacrifice thy food unto the hungry knight?";
		questions[15] = "During battle thou art ordered to guard thy commander's empty tent.  The battle goes poorly and thou \nDost yearn to aid thy fellows.  \nDost thou A) Valiantly enter the battle to aid thy companions, or B) Honor thy post as guard?";
		questions[16] = "A local bully pushes for a fight.  \nDost thou A) Valiantly trounce the rogue, or B) Decline, knowing in thy Spirit that no lasting good will come of it?";
		questions[17] = "Although a teacher of music, thou art a skillful wrestler.  Thou hast been asked to fight in a local championship. \nDost thou A) accept the invitation and Valiantly fight to win, or B) Humbly decline knowing thou art sure to win?";
		questions[18] = "During a pitched battle, thou Dost see a fellow desert his post, endangering many.  As he flees, he is set upon by several enemies. \nDost thou A) Justly let him fight alone, or B) Risk Sacrificing thine own life to aid him?";
		questions[19] = "Thou hast sworn to do thy Lord's bidding in all.  He covets a piece of land and orders the owner removed.  \nDost thou A) serve Justice refusing to act, thus being disgraced, or B) Honor thine oath and unfairly evict the landowner?";
		questions[20] = "Thou Dost believe that virtue resides in all people.  Thou \nDost see a rogue steal from thy Lord.  \nDost thou A) call him to Justice, or B) personally try to sway him back to the Spiritual path of good?";
		questions[21] = "Unwitnessed, thou hast slain a great dragon in self defense.  A poor warrior claims the offered reward.  \nDost thou A) Justly step forward to claim the reward, or B) Humbly go about life, secure in thy self esteem?";
		questions[22] = "Thou art a bounty hunter sworn to return an alleged murder.  After his capture thou believest him to be innocent.  \nDost thou A) Sacrifice thy sizable bounty for thy belief, or B) Honor thy oath to return him as thou hast promised?";
		questions[23] = "Thou hast spent thy life in charitable and righteous work.  Thine uncle the innkeeper lies ill and asks you to take over his tavern.  \nDost thou A) Sacrifice thy life of purity to aid thy kin, or B) decline & follow thy Spirit's call?";
		questions[24] = "Thou art an elderly, wealthy eccentric. Thy end is near. \nDost thou A) donate all thy wealth to feed hundreds of starving children, and receive public adulation, or B) Humbly live out thy life, willing thy fortune to thy heirs?";
		questions[25] = "In thy youth thou pledged to marry thy sweetheart.  Now thou art on a sacred quest in distant lands.  Thy sweetheart asks thee to keep thy vow.  \nDost thou A) Honor thy pledge to wed, or B) follow thy Spiritual crusade?";
		questions[26] = "Thou art at a crossroads in thy life. \nDost thou A) Choose the Honorable life of a Paladin, striving for Truth and Courage, or B) Choose the Humble life of a Shepherd, and a world of simplicity and peace?";
		questions[27] = "Thy parents wish thee to become an apprentice. Two positions are available. \nDost thou A) Become an acolyte in the Spiritual order, or B) Become an assistant to a humble village cobbler?";
	
		script[0] = "The day is warm, yet there is a cooling breeze.  The latest in a series of personal crises seems insurmountable. You are being pulled apart in all directions.";
		script[1] = "Yet this afternoon walk in the country- side slowly brings relaxation to your harried mind.  The soil and strain of modern high-tech living begins to wash off in layers. That willow tree near the stream looks comfortable and inviting.";
		script[2] = "The buzz of dragonflies and the whisper of the willow's swaying branches bring a deep peace.  Searching inward for tranquility and happiness, you close your eyes.";
		script[3] = "A high-pitched cascading sound like crystal wind-chimes impinges on your floating awareness.  As you open your eyes, you see a shimmering blueness rise from the ground.  The sound seems to be emanating from this glowing portal.";
		script[4] = "It is difficult to look at the blueness. Light seems to bend and distort around it, while the sound waves become so intense, they appear to become visible.";
		script[5] = "The portal hangs there for a moment; then, with the rush of an imploding vacuum, it sinks into the ground. Something remains suspended in mid-air for a moment before falling to earth with a heavy thud.";
		script[6] = "Somewhat shaken by this vision, you rise to your feet to investigate.  A crude circle of stones surrounds the spot where the portal appeared. There is something glinting in the grass.";
		script[7] = "You pick up an amulet shaped like a cross with a loop at the top.  It is an Ankh, the sacred symbol of life and rebirth.  But this could not have made the thud, so you look again and find a large book wrapped in thick cloth!";
		script[8] = "With trembling hands you unwrap the book.  Behold, the cloth is a map, and within lies not one book, but two.  The map is of a land strange to you, and the style speaks of ancient cartography.";
		script[9] = "The script on the cover of the first book is arcane but readable.  The title is:\nThe History of Britannia\nas told by\nKyle the Younger";
		script[10] = "The other book is disturbing to look at. Its small cover appears to be fashioned out of some sort of leathery hide, but from what creature is uncertain.  The reddish-black skin radiates an intense aura suggestive of ancient power.";
		script[11] = "The tongue of the title is beyond your ken.  You dare not open the book and disturb whatever sleeps within.  You decide to peruse the History.  Settling back under the willow tree, you open the book.";
		script[12] = "(You read the Book of History)";
		script[13] = "(No, really! Read the Book of History!)";
		script[14] = "Closing the book, you again pick up the Ankh.  As you hold it, you begin to hear a hauntingly familiar, lute-like sound wafting over a nearby hill.  Still clutching the strange artifacts, you rise unbidden and climb the slope.";
		script[15] = "In the valley below you see what appears to be a fair.  It seems strange that you came that way earlier and noticed nothing.  As you mull this over, your feet carry you down towards the site.";
		script[16] = "This is no ordinary travelling carnival, but a Renaissance Fair.  The pennants on the tent tops blow briskly in the late afternoon breeze.";
		script[17] = "The ticket taker at the RenFair's gate starts to ask you for money, but upon spotting your Ankh says, \"Welcome, friend.  Enter in peace and find your path.\"";
		script[18] = "The music continues to pull you forward amongst the merchants and vendors.  Glimpses of fabulous treasures can be seen in some of the shadowy booths.";
		script[19] = "These people are very happy.  They seem to glow with an inner light.  Some look up as you pass and smile, but you cannot stop - the music compels you to move onward through the crowd.";
		script[20] = "Through the gathering dusk you see a secluded gypsy wagon sitting off in the woods.  The music seems to emanate from the wagon.  As you draw near, a woman's voice weaves into the music, saying: \"You may approach, O seeker.\"";
		script[21] = "You enter to find an old gypsy sitting in a small curtained room.  She wears an Ankh around her neck. In front of her is a round table covered in deep green velvet.  The room smells so heavily of incense that you feel dizzy.";
		script[22] = "Seeing the Ankh, the ancient gypsy smiles and warns you never to part with it.  \"We have been waiting such a long time, but at last you have come.  Sit here and I shall read the path of your future.\"";
		script[23] = "Upon the table she places a curious wooden object like an abacus but without beads.  In her hands she holds eight unusual cards.  \"Let us begin the casting.\"";
		script[24] = "The gypsy places the first two cards ";
		script[25] = "The gypsy places two more of the cards ";
		script[26] = "The gypsy places the last two cards upon the table.  They are the cards of Honesty, Compassion, Valor, Justice, Sacrifice, Honor, Spirituality and Humility";
		script[27] = "With the final choice, the incense swells up around you.  The gypsy speaks as if from a great distance, her voice growing fainter with each word: \"So be it!  Thy path is chosen!\"";
		script[28] = "There is a moment of intense, wrenching vertigo.  As you open your eyes, a voice whispers within your mind, \"Seek the counsel of thy sovereign.\"  After a moment, the spinning subsides, and you open your eyes to...";
	
	}
	
	TextureAtlas atlas;
	Animation beast1;
	Animation beast2;
	
	
	public Intro() {
		atlas = new TextureAtlas(Gdx.files.classpath("graphics/tile-atlas.txt"));

	}


	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void render(float delta) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
	

	

}
