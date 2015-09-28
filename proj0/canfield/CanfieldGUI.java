package canfield;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import ucb.gui.LayoutSpec;
import ucb.gui.TopLevel;

/**
 * A top-level GUI for Canfield solitaire.
 *
 * @author
 */
class CanfieldGUI extends TopLevel implements GameListener {

    /** A new window with given TITLE and displaying GAME. */
    CanfieldGUI(String title, Game game) {
        super(title, true);
        this._game = game;
        game.addListener(this);
        
        this.addMenuButton("Game->New Game", "newGame");
        this.addMenuButton("Game->Undo", "undo");
        this.addMenuButton("Game->Quit", "quit");
        


        
        this.addLabel("New game started!","messageLabel", new LayoutSpec("y",
                1, "x", 0));
        
        this._display = new GameDisplay(game);
        this.add(this._display, new LayoutSpec("y", 0, "width", 2));
        this._display.setMouseHandler("click", this, "mouseClicked");
        this._display.setMouseHandler("release", this, "mouseReleased");
        this._display.setMouseHandler("drag", this, "mouseDragged");
        
        

        this.display(true);
    }

    /**
     * Creates a new game
     * @param dummy
     */
    public void newGame(String dummy){
    	//TODO: IMOPLEMENRT
    }
    
    /**
     * Undoes a move if there is one to undo.
     * @param dummy
     */
    public void undo(String dummy){
    	try{
    		_game.undo();
    	}
    	catch(IllegalArgumentException exp){
    		this.error(exp);
    	}
    	
    	this._display.repaint();
    }
    
    /** Respond to "Quit" button. */
    public void quit(String dummy) {
        System.exit(1);
    }
    
    
    
    /* =========== GAME LISTENER ===============*/
    @Override
    public void onGameChange(Game changedGame) {
        this._display.rebuild();
    }
    
    /* =========== INPUT LISTENER ==============*/

    /** Action in response to mouse-clicking event EVENT. */
    public synchronized void mouseClicked(MouseEvent event) {
        GUICard top = _display.getTopCardAt(event.getPoint());
        if(top != null){

            if(top.getType() == CardType.STOCK)
                _game.stockToWaste();

            top.onClick(event.getPoint());
        }

        this._display.repaint();
    }



    /** Action in response to mouse-dragging event EVENT. */
    public synchronized void mouseDragged(MouseEvent event) {
        if(selectedCard == null){
            this.selectedCard = _display.getTopCardAt(event.getPoint());
            if(selectedCard != null){
                this.selectedLayer = selectedCard.getLayer();
                selectedCard.setLayer(-1);
            }
        }
        
        
        if(selectedCard != null)
            selectedCard.onDrag(event.getPoint());
        
        
        this._display.repaint();
    }
    
    /** Action in response to mouse-released event EVENT.
     * Occurs only after drag.
     */
    public synchronized void mouseReleased(MouseEvent event) {
        message("released");
       
        if(selectedCard != null){
            
            /*see if there was another card. */
            GUICard other = _display.getCollision(selectedCard);
            if(other != null){

                try{
                    /* THE LOGIC FOR SELECTED CARD -> OTHER */
                    switch(selectedCard.getType()){
                    
                    /* WASTE TO (FOUNDATION, TABLEAU) */
                    case WASTE:
                        
                        switch(other.getType()){
                        case FOUNDATION:
                            _game.wasteToFoundation();
                            break;
                        case TABLEAU_BASE:
                        case TABLEAU_HEAD:
                        case TABLEAU_NORM:
                            _game.wasteToTableau(_game.tableauPileOf(other.getRepr()));
                            break;
                        }
                        break;
                    
                    /* TABLEAU_HEAD TO(FOUNDATION)*/
                    case TABLEAU_HEAD:
                        
                        if(other.getType() == CardType.FOUNDATION)
                            _game.tableauToFoundation(_game.tableauPileOf(selectedCard.getRepr()));
                        break;
                    /* TABLEAU_BASE TO (TABLEAU_*) */
                    case TABLEAU_BASE:
                        int tabPile = _game.tableauPileOf(selectedCard.getRepr());
                        switch(other.getType()){
                        case TABLEAU_BASE:
                        case TABLEAU_HEAD:
                        case TABLEAU_NORM:
                            _game.tableauToTableau(
                                    tabPile,
                                    _game.tableauPileOf(other.getRepr()));
                            break;
                        case FOUNDATION:
                            /* In the case that the base is the only element in the pile 
                             * we can clearly move the pile up to the foundation.
                             */
                            if(_game.tableauSize(tabPile) == 1)
                                _game.tableauToFoundation(tabPile);
                            break;
                        }
                        break;
                        
                    /* FOUNDATION TO (TABLEAU_*) */
                    case FOUNDATION:
                        switch(other.getType()){
                        case TABLEAU_BASE:
                        case TABLEAU_HEAD:
                        case TABLEAU_NORM:
                            _game.foundationToTableau(
                                    _game.foundationPileOf(selectedCard.getRepr()),
                                    _game.tableauPileOf(other.getRepr()));
                            break;
                        }
                        break;
                    }
                } catch(IllegalArgumentException exp){
                    this.error(exp);
                }
                
            }
                selectedCard.onRelease(event.getPoint());
            
            
            selectedCard.setLayer(selectedLayer);
            selectedCard = null;
        }
        
        this._display.repaint();
    }
    

    
    /* ================ MESSAGE STUFF ===================*/
    /**
     * Writes an error message to the label.
     * @param exp
     */
    private void error(Exception exp){
    	String errorMsg = (String.format(exp.getMessage()));
    	this.message( "Error" + errorMsg);
    	this.showMessage(errorMsg, "Error", "Error");
    	
    }
    
    /**
     * Writes a simple message to the label.
     * @param message
     */
    private void message(String message){
    	this.setLabel("messageLabel", message);
    }
    
    
    /*===================================================*/
    /** The board widget. */
    private final GameDisplay _display;

    /** The game I am consulting. */
    private final Game _game;
    
    /** The card I am selectiong **/
    private GUICard selectedCard = null;
    private int selectedLayer = 0;

}
