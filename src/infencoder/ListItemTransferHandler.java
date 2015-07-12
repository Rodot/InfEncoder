/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infencoder;

//@camickr already suggested above.

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.MOVE;

//http://docs.oracle.com/javase/tutorial/uiswing/dnd/dropmodedemo.html
class ListItemTransferHandler extends TransferHandler {
  private final DataFlavor localObjectFlavor;
  private Object[] transferedObjects = null;
  public ListItemTransferHandler() {
    localObjectFlavor = new ActivationDataFlavor(
      Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
  }
  @SuppressWarnings("deprecation")
  @Override protected Transferable createTransferable(JComponent c) {
    JList list = (JList) c;
    indices = list.getSelectedIndices();
    transferedObjects = list.getSelectedValues();
    return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
  }
  @Override public boolean canImport(TransferHandler.TransferSupport info) {
    if(!info.isDrop() || !info.isDataFlavorSupported(localObjectFlavor)) {
      return false;
    }
    return true;
  }
  @Override public int getSourceActions(JComponent c) {
    return MOVE; //TransferHandler.COPY_OR_MOVE;
  }
  @SuppressWarnings("unchecked")
  @Override public boolean importData(TransferHandler.TransferSupport info) {
    if(!canImport(info)) {
      return false;
    }
    JList target = (JList)info.getComponent();
    JList.DropLocation dl = (JList.DropLocation)info.getDropLocation();
    DefaultListModel listModel = (DefaultListModel)target.getModel();
    int index = dl.getIndex();
    int max = listModel.getSize();
    if(index<0 || index>max) {
      index = max;
    }
    addIndex = index;
    try {
      Object[] values = (Object[])info.getTransferable().getTransferData(
          localObjectFlavor);
      addCount = values.length;
      for(int i=0; i<values.length; i++) {
        int idx = index++;
        listModel.add(idx, values[i]);
        target.addSelectionInterval(idx, idx);
      }
      return true;
    } catch(UnsupportedFlavorException ufe) {
      ufe.printStackTrace();
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
    return false;
  }
  @Override protected void exportDone(
      JComponent c, Transferable data, int action) {
    cleanup(c, action == MOVE);
  }
  private void cleanup(JComponent c, boolean remove) {
    if(remove && indices != null) {
      JList source = (JList)c;
      DefaultListModel model = (DefaultListModel)source.getModel();
      if(addCount > 0) {
        //http://java-swing-tips.googlecode.com/svn/trunk/DnDReorderList/src/java/example/MainPanel.java
        for(int i=0; i<indices.length; i++) {
          if(indices[i]>=addIndex) {
            indices[i] += addCount;
          }
        }
      }
      for(int i=indices.length-1; i>=0; i--) {
        model.remove(indices[i]);
      }
    }
    indices  = null;
    addCount = 0;
    addIndex = -1;
  }
  private int[] indices = null;
  private int addIndex  = -1; //Location where items were added
  private int addCount  = 0;  //Number of items added.
}