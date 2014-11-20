package ultima;

import java.util.Observable;
import java.util.Observer;

import objects.Party;
import objects.Party.PartyMember;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class DialogWindow extends Window implements Observer {
	
	private Ultima4 mainGame;
	
	public static int width = 120;
	public static int height = 120;
	
	private Label topText;
	private Label middleText;
	private TextField input;
	private LogScrollPane scrollPane;
	
	private boolean collapsed;
	private float collapseHeight = 20f;
	private float expandHeight;
	
	public DialogWindow(final Stage stage, Ultima4 mainGame, Skin skin) {
		super("", skin);

		this.mainGame = mainGame;
		
		addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (getTapCount() == 2 && getHeight() - y <= getPadTop() && y < getHeight() && x > 0 && x < getWidth())
					toggleCollapsed();
			}
		});
		
		topText = new Label("",skin,"gray-background");
		topText.setAlignment(Align.topLeft);

		middleText = new Label("",skin,"gray-background");

		setPartyText(mainGame.party); 

		scrollPane = new LogScrollPane(skin, width);
		for (int i=0;i<4;i++) 
			scrollPane.add("dogs and cats do not always play well together.");
		
		input = new TextField("",skin);
		

				
		defaults().padTop(2).padBottom(2).padLeft(2).padRight(2).left();
		
		Table top = new Table();
		//top.defaults().padTop(0).padBottom(0).padLeft(0).padRight(0).left();
		top.add(topText).maxWidth(width).width(width).maxHeight(height).height(height);	
		add(top);
		row();
		
		Table middle = new Table();
		//middle.defaults().padTop(0).padBottom(0).padLeft(0).padRight(0).left();
		middle.add(middleText).maxWidth(width).width(width);	
		add(middle);
		row();
		
		Table bottom = new Table();
		//bottom.defaults().padTop(0).padBottom(0).padLeft(0).padRight(0).left();
		bottom.add(scrollPane).maxWidth(width).width(width).maxHeight(height).height(height);	
		add(bottom);
		row();

		add(input).maxWidth(width).width(width);

		pack();

		stage.addActor(this);
		
		setPosition(700, 500);


	}
	
	public void setPartyText(Party party) {
		StringBuffer sb = new StringBuffer();
		int index=1;
		for(PartyMember pm : party.getMembers()) {
			sb.append(index + "-" +pm.getPlayer().name + "   " + pm.getPlayer().hp + "" + pm.getPlayer().status.getValue());
			index++;
		}
		topText.setText(sb.toString());
		
		middleText.setText("F: " +party.getSaveGame().food + "    G: " +party.getSaveGame().gold);
	}
	
	public void expand () {
		if (!collapsed) return;
		setHeight(expandHeight);
		setY(getY() - expandHeight + collapseHeight);
		collapsed = false;
	}

	public void collapse () {
		if (collapsed) return;
		expandHeight = getHeight();
		setHeight(collapseHeight);
		setY(getY() + expandHeight - collapseHeight);
		collapsed = true;
		if (getStage() != null) getStage().setScrollFocus(null);
	}

	public void toggleCollapsed () {
		if (collapsed)
			expand();
		else
			collapse();
	}

	public boolean isCollapsed () {
		return collapsed;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		setPartyText(mainGame.party); 
	}
	


}
