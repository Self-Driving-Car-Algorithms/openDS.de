package cz.cvut.cognitive.distractors;

import com.jme3.input.InputManager;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.ViewPort;
import de.lessvoid.nifty.Nifty;
import eu.opends.car.SteeringCar;
import eu.opends.main.Simulator;

/**
 *
 * @author Johnny
 * 
 * Class for Text-type distraction. When this option is selected in options
 * there is always (set) probability to show question in middle of screen which
 * requires answering. Simulation pauses till question is answered or canceled.
 * 
 */
public class TextDistraction extends DistractionClass {
    
    private final InputManager inputManager;
    private final Nifty nifty;
    private final NiftyJmeDisplay niftyDisplay;
    private final Simulator sim;
    private boolean questionOnScreen = false;
    private final ViewPort guiViewPort;  
    private final TextDistractionController controller;
    private boolean textOn = false;
    private SteeringCar car;
    public float COG_SCORE;
    
    /**
     *Constructor for TextDistraction
     *@param: sim - simulator          
     */
    public TextDistraction(Simulator sim){

                this.sim = sim;
                this.car = sim.getCar();
		inputManager = sim.getInputManager();
		guiViewPort = sim.getGuiViewPort();
                               
                //creates custom screen
                niftyDisplay = new NiftyJmeDisplay(sim.getAssetManager(), inputManager, 
				sim.getAudioRenderer(), guiViewPort);
                
                nifty = niftyDisplay.getNifty();
                
                String xmlPath = "Interface/TextDistractionGUI.xml";
                
                //adds new custom controller
                controller = new TextDistractionController(this, sim);
		// Read XML and initialize custom ScreenController
		nifty.fromXml(xmlPath, "start",	controller);
                COG_SCORE = 3;
                
    }
    
    public Nifty getNifty()
	{
		return nifty;
	}
    
    /**
     * Update function: if preset probability of Question appearing is higher than
     * random generated number (1-100), TextBox will appear. 
     * 
     */
    @Override
    public void update(float tpf, float propability) {
        int n = (int)(Math.random() * 100) + 1;
        if (n <= propability){
            controller.sendToScreen(); 
            car.getCarControl().setLinearVelocity(Vector3f.ZERO);
            car.getCarControl().setAngularVelocity(Vector3f.ZERO);
            car.getCarControl().resetSuspension();
            car.setSteeringWheelState(0);
            
            sim.setPause(true);
            DistractionSettings.setQuestionAnswered(false);
            showDialog();
            textOn = true;
            CognitiveFunction.distScore += COG_SCORE;
            CognitiveFunction.activeDistCount++;
            CognitiveFunction.activeDistNames[5] = 1;
            DistractionSettings.distRunning++;
        }   
    }
    
    /**
     * Remove function: if distractor was spawned before this call, it will be
     * removed from the scene.
     */
    @Override
    public void remove() {
        if (textOn){
            DistractionSettings.setQuestionAnswered(true);
            DistractionSettings.distRunning--;
            hideDialog();
            CognitiveFunction.distScore -= COG_SCORE;
            CognitiveFunction.activeDistCount--;
            CognitiveFunction.activeDistNames[5] = 0;
            textOn = false;
        }
    }
    
    /**
     * Pops dialog on the screen with option to answer
     */
    public void showDialog() 
	{
		if(!questionOnScreen)
		{				
			// attach the Nifty display to the gui view port as a processor
			guiViewPort.addProcessor(niftyDisplay);
			inputManager.setCursorVisible(true);
			
			questionOnScreen = true;
		}
	}
    
    /**
     * Hides dialog from the screen, unpauses simulation.
     */
    public void hideDialog() 
	{
		if(questionOnScreen)
		{
                    // stops movement of the car
                    car.getCarControl().setLinearVelocity(Vector3f.ZERO);
                    car.getCarControl().setAngularVelocity(Vector3f.ZERO);
                    car.getCarControl().resetSuspension();
                    car.setSteeringWheelState(0);
                    // detach the Nifty display from the gui view port as a processor
                    guiViewPort.removeProcessor(niftyDisplay);
                    inputManager.setCursorVisible(false);
                    sim.setPause(false);
                    questionOnScreen = false;
		}
	}
    
}
