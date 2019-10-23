package ARP;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.ProgressBarUI;

public class FileChatDlg extends JFrame implements BaseLayer {
   public int nUpperLayerCount = 0;
   public String pLayerName = null;
   public BaseLayer p_UnderLayer = null;
   public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
   BaseLayer UnderLayer;
   String fn = "";
   String file_name = null;
   int k = 0; // proxy entry table index

   private static LayerManager m_LayerMgr = new LayerManager();

   private JTextField ChattingWrite;

   Container contentPane;

   JTextArea ChattingArea;
   JTextArea srcAddress;
   JTextArea dstAddress;
   JTextArea Entry;

   JLabel lblsrc;
   JLabel lbldst;

   JButton Setting_Button;
   JButton Chat_send_Button;
   JButton transButton;
   JButton selectButton;
   JButton all_delete;
   JButton Entry_add;
   JButton Entry_remove;

   JProgressBar upperProgressBar;
   JProgressBar underProgressBar;

   static JComboBox<String> NICComboBox;

   int adapterNumber = 0;

   String Text;
   String pathString;
   String macAddress;

   public static void main(String[] args) throws IOException {
      // TODO Auto-generated method stub
      m_LayerMgr.AddLayer(new NILayer("NI"));

      m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));

      m_LayerMgr.AddLayer(new ARPLayer("ARP"));

      m_LayerMgr.AddLayer(new TCPLayer("TCP"));

      m_LayerMgr.AddLayer(new IPLayer("IP"));

      m_LayerMgr.AddLayer(new FileChatDlg("GUI"));

//      m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *Chat ( *GUI ) ) )");
//      m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *ARP ( *IP ) *IP ( *TCP ( *GUI ) ) ) )");
//      m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *ARP ( *IP ( *TCP ( *GUI ) ) ) *IP ( *TCP ( *GUI ) ) ) )");
      m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *IP ( *TCP ( *GUI ) ) *ARP ( *IP ( *TCP ( *GUI ) ) ) ) )");
      
      ((NILayer)m_LayerMgr.GetLayer("NI")).SetAdapterNumber();
   }

   public FileChatDlg(String pName) {
      pLayerName = pName;

      setTitle("file/chat");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(250, 250, 644, 425);
      contentPane = new JPanel();
      ((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);

      JPanel chattingPanel = new JPanel();// chatting panel
      chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Proxy Cache",
            TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      chattingPanel.setBounds(10, 5, 360, 360);
      contentPane.add(chattingPanel);
      chattingPanel.setLayout(null);

      JPanel chattingEditorPanel = new JPanel();// chatting write panel
      chattingEditorPanel.setBounds(10, 15, 340, 210);
      chattingPanel.add(chattingEditorPanel);
      chattingEditorPanel.setLayout(null);

      ChattingArea = new JTextArea();
      ChattingArea.setEditable(false);
      ChattingArea.setBounds(0, 0, 340, 210);
      chattingEditorPanel.add(ChattingArea);// chatting edit

      ChattingWrite = new JTextField();
      ChattingWrite.setBounds(90, 300, 150, 30);// 249
      chattingPanel.add(ChattingWrite);
      ChattingWrite.setColumns(10);// writing area

      JButton item_delete = new JButton("Item Delete");
      item_delete.setBounds(60, 250, 100, 30);
      chattingPanel.add(item_delete);
      item_delete.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            ChattingWrite.setText("");

         }
      });

      all_delete = new JButton("All Delete");
      all_delete.setBounds(180, 250, 100, 30);
      chattingPanel.add(all_delete);
      all_delete.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            ChattingArea.setText("");
            ((ARPLayer)m_LayerMgr.GetLayer("ARP")).del_all();
            areaTable = new String[0];
         }
      });

      JLabel ip_address = new JLabel("IP");
      ip_address.setBounds(30, 300, 40, 30);
      chattingPanel.add(ip_address);

      JPanel entryPanel = new JPanel();
      entryPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Proxy ARP Entry",
            TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      entryPanel.setBounds(380, 5, 236, 371);
      contentPane.add(entryPanel);
      entryPanel.setLayout(null);

      Entry = new JTextArea();
      Entry.setEditable(false);
      Entry.setBounds(5, 20, 225, 180);
      entryPanel.add(Entry);

      Entry_add = new JButton("Add");
      Entry_add.setBounds(30, 220, 80, 30);
      entryPanel.add(Entry_add);
      Entry_add.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            class newWindow extends JFrame {
               Container windowPanel;
               JTextField ip_address;
               JTextField mac_address;
               JPanel ip_add;
               JPanel mac_add;
               JPanel buttons;
               JPanel all;
               JLabel ip;
               JLabel mac;
               JButton ok;
               JButton no;

               public newWindow() {
                  setTitle("new window");
                  this.setSize(300, 300);
                  this.setLocation(400, 400);
                  this.setVisible(true);
                  ip_add = new JPanel();
                  ip_add.setLayout(new BoxLayout(ip_add, BoxLayout.X_AXIS));
                  this.add(ip_add);
                  ip = new JLabel("IP Address");
                  ip_add.add(ip);
                  ip_address = new JTextField();
                  ip_add.add(ip_address);
                  mac_add = new JPanel();
                  mac_add.setLayout(new BoxLayout(mac_add, BoxLayout.X_AXIS));
                  mac_address = new JTextField();
                  mac = new JLabel("Mac Address");
                  mac_add.add(mac);
                  mac_add.add(mac_address);
                  buttons = new JPanel();
                  buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
                  ok = new JButton("OK");
                  ok.addActionListener(new ActionListener() {

                     @Override
                     public void actionPerformed(ActionEvent e) {
                        // TODO Auto-generated method stub
                        ((ARPLayer) m_LayerMgr.GetLayer("ARP")).Proxy_Add_ipAndMac_addr(ip_address.getText(),
                              hexStringToByteArray(mac_address.getText()));
                        String iptemp, mactemp, result = "";
                           iptemp = byteArrayToIPString(
                                 ((ARPLayer) m_LayerMgr.GetLayer("ARP")).proxyTable[k].ip_addr.addr);
                           mactemp = byteArrayToHexString(
                                 ((ARPLayer) m_LayerMgr.GetLayer("ARP")).proxyTable[k].mac_addr.addr);
                           result += iptemp + " " + mactemp + "\n";
                           Entry.append(result);
                           k++;
                           dispose();
                        }
                  });
                  no = new JButton("Cancel");
                  no.addActionListener(new ActionListener() {

                     @Override
                     public void actionPerformed(ActionEvent e) {
                        // TODO Auto-generated method stub
                        dispose();
                     }
                  });
                  buttons.add(ok);
                  buttons.add(no);
                  all = new JPanel();
                  all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
                  all.add(ip_add);
                  all.add(mac_add);
                  all.add(buttons);
                  this.add(all);
               }
            }
            newWindow nw = new newWindow();
         }
      });

      Entry_remove = new JButton("Remove");
      Entry_remove.setBounds(125, 220, 80, 30);
      Entry_remove.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stu
            ((ARPLayer)m_LayerMgr.GetLayer("ARP")).Proxy_Del_ipAndMac_addr();
            k = 0;
            Entry.setText("");
         }
      });
      entryPanel.add(Entry_remove);
      JTextField Gratitous;
      JPanel Grat = new JPanel();
      Grat.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gratitous ARP", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      entryPanel.add(Grat);
      Grat.setBounds(10, 260, 220, 100);
      Gratitous = new JTextField();
      JLabel Grat_label = new JLabel("H/W");
      Grat.add(Grat_label);
      Grat_label.setBounds(10, 50, 30, 50);
      Grat.add(Gratitous);
      Gratitous.setBounds(50, 50, 100, 80);
      Gratitous.setColumns(15);
      JButton Grat_Send = new JButton("Send");
      Grat.add(Grat_Send);
      Grat_Send.setBounds(50, 100, 50, 50);
      Grat_Send.addActionListener(new ActionListener() {
         
         @Override
         public void actionPerformed(ActionEvent e) {
            String hwString = Gratitous.getText();
            byte[] grat_input = hexStringToByteArray(hwString);
            ((ARPLayer)m_LayerMgr.GetLayer("ARP")).getNewMacAddr(grat_input);
         }
      });
      

      JLabel lblNic = new JLabel("NIC \uC120\uD0DD");
      lblNic.setBounds(10, 22, 170, 20);
      entryPanel.add(lblNic);

      JComboBox comboBox = new JComboBox();
      JComboBox fileBox = new JComboBox();

      int m = ((NILayer) m_LayerMgr.GetLayer("NI")).m_pAdapterList.size();

      for (int i = 0; i < m; i++) {
         comboBox.addItem(((NILayer) m_LayerMgr.GetLayer("NI")).m_pAdapterList.get(i).getDescription());
      }

      comboBox.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            int m = ((NILayer) m_LayerMgr.GetLayer("NI")).m_pAdapterList.size();
            JComboBox temp = (JComboBox) e.getSource();
            String str = temp.getSelectedItem().toString();

            for (int i = 0; i < m; i++) {
               if (str.equals((((NILayer) m_LayerMgr.GetLayer("NI")).m_pAdapterList).get(i).getDescription())) {
                  adapterNumber = i;
               }
            }

            String st = new String();
            try {
               st = byteArrayToHexString(((NILayer) m_LayerMgr.GetLayer("NI")).m_pAdapterList.get(adapterNumber)
                     .getHardwareAddress());
            } catch (IOException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
            macAddress = st;
            // srcAddress.setText(st);
         }
      });

      comboBox.setBounds(10, 300, 170, 23);
      // entryPanel.add(comboBox);

      Chat_send_Button = new JButton("Send");
      Chat_send_Button.setBounds(260, 300, 80, 30);
      Chat_send_Button.addActionListener(new setAddressListener());
      chattingPanel.add(Chat_send_Button);// chatting send button

      setVisible(true);
   }

   class setAddressListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
         if (e.getSource() == all_delete) {
            ChattingArea.append("all_delete");
         }

         if (e.getSource() == Chat_send_Button) {
            // String s = ChattingWrite.getText();
            // byte[] b = s.getBytes();
//               ((ChatAppLayer) m_LayerMgr.GetLayer("Chat")).Send(b, b.length);
            // ChattingArea.append("SEND : " + s + "\n");
            try {
               ((TCPLayer) m_LayerMgr.GetLayer("TCP")).SetIpSrcAddress(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
            ((TCPLayer) m_LayerMgr.GetLayer("TCP")).SetIpDstAddress(ChattingWrite.getText());
            byte[] temp = { 0, 0, 0, 0 };
            ((TCPLayer) m_LayerMgr.GetLayer("TCP")).Send(temp, temp.length);
         }
         // 占쎈솁占쎌뵬 select 占쎈릭占쏙옙占쎌뱽占쎈르
         if (e.getSource() == selectButton) {
            JFileChooser fs = new JFileChooser(new File("c:\\"));
            fs.setDialogTitle("Open a File");
            int result = fs.showOpenDialog(null);
            underProgressBar.setValue(0);

            if (result == JFileChooser.APPROVE_OPTION) {
               try {
                  File fi = fs.getSelectedFile();
                  pathString = fi.getPath(); // 占쎈솁占쎌뵬 野껋럥以� String
                  fn = fi.getName();
                  upperProgressBar.setStringPainted(true);
                  upperProgressBar.setString(pathString);
                  System.out.println(pathString);
               } catch (Exception e2) {
                  // TODO: handle exception
                  JOptionPane.showMessageDialog(null, "占쎈솁占쎌뵬占쎌뱽 占쎈연占쎈뮉占쎈쑓 占쎈뼄占쎈솭占쎈뻥占쎈뮸占쎈빍占쎈뼄.", "占쎌궎�몴占� 筌롫뗄�뻻筌욑옙", JOptionPane.WARNING_MESSAGE);
               }
            }
         }

         if (e.getSource() == transButton) {
            Progress_Thread thread = new Progress_Thread();
            Thread object = new Thread(thread);
            object.start();
         }

      }
   }

   public boolean Receive(byte[] input) {
      String s = new String(input);
      ChattingArea.append("RECV : " + s + "\n");
      return true;
   }

   @Override
   public void SetUnderLayer(BaseLayer pUnderLayer) {
      // TODO Auto-generated method stub
      if (pUnderLayer == null)
         return;
      this.p_UnderLayer = pUnderLayer;
   }

   @Override
   public void SetUpperLayer(BaseLayer pUpperLayer) {
      // TODO Auto-generated method stub
      if (pUpperLayer == null)
         return;
      this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
   }

   @Override
   public String GetLayerName() {
      // TODO Auto-generated method stub
      return pLayerName;
   }

   @Override
   public BaseLayer GetUnderLayer() {
      // TODO Auto-generated method stub
      if (p_UnderLayer == null)
         return null;
      return p_UnderLayer;
   }

   @Override
   public BaseLayer GetUpperLayer(int nindex) {
      // TODO Auto-generated method stub
      if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
         return null;
      return p_aUpperLayer.get(nindex);
   }

   @Override
   public void SetUpperUnderLayer(BaseLayer pUULayer) {
      this.SetUpperLayer(pUULayer);
      pUULayer.SetUnderLayer(this);

   }

   public static byte[] hexStringToByteArray(String s) {
      int len = s.length();
      byte[] data = new byte[len / 2];

      for (int i = 0; i < len; i += 2) {
         data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
      }

      return data;
   }

   // 占쎈땾占쎌젟占쎈쭆
   String[] areaTable = new String[0];

   public void setChattingArea(byte[] ip_addr, byte[] mac_iddr, String status, int index) {// �빊遺쏙옙 0, 占쎌젫椰꾬옙 1, 癰귨옙野껓옙 2
      ChattingArea.setText("");
      String ip = byteArrayToHexString_ip_mac(ip_addr);
      String mac = "??????????";
      if (mac_iddr != null)
         mac = byteArrayToHexString_ip_mac(mac_iddr);
      String result = ip + " " + mac + " " + status;

      if (index == 0) {
         this.add_areaTable(result);
      } else if (index == 1) {
         this.del_areaTable(ip);
      } else {
         this.change_areaTable(ip, result);
      }
      result = "";
      for (int i = 0; i < areaTable.length; i++)
         result += areaTable[i] + "\n";
      ChattingArea.append(result);
   }

   public void change_areaTable(String ip_addr, String contents) {
      for (int i = 0; i < areaTable.length; i++) {
         if (areaTable[i].contains(ip_addr))
            areaTable[i] = contents;
      }
   }

   public void add_areaTable(String contents) {
      String[] temp = new String[areaTable.length + 1];
      for (int i = 0; i < areaTable.length; i++)
         temp[i] = areaTable[i];
      temp[temp.length - 1] = contents;
      areaTable = temp.clone();
   }

public void del_areaTable(String ip_addr) {
		if (areaTable.length > 1) {
			String[] temp = new String[areaTable.length - 1];
			for (int i = 0, j = 0; i < areaTable.length; i++, j++) {
				if (areaTable[i].contains(ip_addr)) {
					j--;
					continue;
				}
				temp[j] = areaTable[i];
			}
			areaTable = temp.clone();
		}
		else {
			areaTable = new String[0];
		}
	}
   public static String byteArrayToHexString_ip_mac(byte[] bytes) {
      StringBuilder sb = new StringBuilder();
      String s = ".";
      if(bytes.length == 6) {
         s = ":";
         for(int i = 0; i < bytes.length - 1; i++)
            sb.append(String.format("%02X", bytes[i] & 0xff) + s);
         sb.append(String.format("%02X", bytes[bytes.length - 1] & 0xff));
         return sb.toString();
      }
      for(int i = 0; i < bytes.length - 1; i++)
         sb.append((int)(bytes[i]&0xFF) + s);
      sb.append((int)(bytes[bytes.length-1]&0xFF));
      return sb.toString();
   }
   //

   /*
    * //占쎌뵠占쎌읈 �굜遺얜굡 public void setChattingArea(byte[] ip_addr, byte[] mac_iddr, String
    * status) { String ip = byteArrayToHexString(ip_addr); String mac =
    * byteArrayToHexString(mac_iddr); String result = ip+" "+mac+" "+status+"\n";
    * ChattingArea.append(result); }
    */
   
   public static String byteArrayToIPString(byte[] bytes) {
      StringBuilder result = new StringBuilder();
      int temp;
      for(int i = 0; i < bytes.length; i++) {
         temp = bytes[i] & 0xFF;
         if(i != bytes.length-1)
            result.append(Integer.toString(temp)+".");
         else
            result.append(Integer.toString(temp));
      }
      return result.toString();
   }
   public static String byteArrayToHexString(byte[] bytes) {
      StringBuilder sb = new StringBuilder();

      for (byte b : bytes) {
         sb.append(String.format("%02X", b & 0xff));
      }

      return sb.toString();
   }
   

   class Progress_Thread implements Runnable {

      public Progress_Thread() {

      }

      @Override
      public void run() {
//            try {
//               byte[] input = ((FileAppLayer) m_LayerMgr.GetLayer("File")).fileToArray(pathString);
//               ((FileAppLayer) m_LayerMgr.GetLayer("File")).Send(input, input.length, fn);
//               file_name = fn;
//               //((FileAppLayer) m_LayerMgr.GetLayer("File")).fileName = fn;
//            } catch (IOException e1) {
//               // TODO Auto-generated catch block
//               e1.printStackTrace();
//            }
      }
   }

}
