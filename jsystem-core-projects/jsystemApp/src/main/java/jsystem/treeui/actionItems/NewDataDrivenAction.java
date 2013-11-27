package jsystem.treeui.actionItems;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import jsystem.guiMapping.JsystemMapping;
import jsystem.treeui.TestRunner;
import jsystem.treeui.images.ImageCenter;
import jsystem.treeui.teststable.TestsTableController.ActionType;

public class NewDataDrivenAction extends IgnisAction {
	
	private static final long serialVersionUID = 1L;
	
	private static NewDataDrivenAction action;

	
	private NewDataDrivenAction(){
		//TODO: Change it to the correct values
		super();
		putValue(Action.NAME, JsystemMapping.getInstance().getLoopButton());
		putValue(Action.SHORT_DESCRIPTION, JsystemMapping.getInstance().getLoopButton());
		putValue(Action.SMALL_ICON, ImageCenter.getInstance().getImage(ImageCenter.ICON_FOR_LOOP));
		putValue(Action.LARGE_ICON_KEY, ImageCenter.getInstance().getImage(ImageCenter.ICON_FOR_LOOP));
		putValue(Action.ACTION_COMMAND_KEY, "flowcontrol-new-forloop");
	}
	

	public static NewDataDrivenAction getInstance(){
		if (action == null){
			action =  new NewDataDrivenAction();
		}
		return action;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			TestRunner.treeView.getTableController().addFlowControlElement(ActionType.NEW_DATA_DRIVEN);
		} catch (Exception ex) {
		}
	}

}
