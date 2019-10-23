package ARP;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

class Array {
   _IP_ADDR ip_addr;
   _ETHERNET_ADDR mac_addr;

   public Array(_IP_ADDR ip_addr, _ETHERNET_ADDR mac_addr) {
      this.ip_addr = ip_addr;
      this.mac_addr = mac_addr;
   }
}

class _IP_ADDR {
   byte[] addr = new byte[4];
   int del_flag = 0;

   public _IP_ADDR() {
      for (int i = 0; i < 4; i++)
         this.addr[i] = (byte) 0x00;
   }

   public _IP_ADDR(byte[] ip_addr) {
      for (int i = 0; i < 4; i++)
         this.addr[i] = ip_addr[i];
   }
}

class _ETHERNET_ADDR {
   byte[] addr = new byte[6];

   public _ETHERNET_ADDR() {
      for (int i = 0; i < 6; i++)
         this.addr[i] = (byte) 0x00;
   }

   public _ETHERNET_ADDR(byte[] enet_addr) {
      for (int i = 0; i < 6; i++)
         this.addr[i] = enet_addr[i];
   }
}

public class ARPLayer implements BaseLayer {
   public String pLayerName = null;
   public int nUpperLayerCount = 0;
   public BaseLayer p_UnderLayer = null;
   public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
   public Array[] table = new Array[0];
   public Array[] proxyTable = new Array[0]; // proxyTable
   public _IP_ADDR my_ip_addr = new _IP_ADDR();
   public _ETHERNET_ADDR my_enet_addr = new _ETHERNET_ADDR();
   public _ETHERNET_ADDR newMacAddr;
   boolean GratuitousFlag = false;

   public _IP_ADDR[] non_enet = new _IP_ADDR[0];
   public _IP_ADDR[] enet = new _IP_ADDR[0];
   public int del_flag = 0;

   private class _ARP_HEADER {
      _IP_ADDR ip_dstaddr;
      _IP_ADDR ip_srcaddr;

      _ETHERNET_ADDR enet_dstaddr;
      _ETHERNET_ADDR enet_srcaddr;

      byte[] hard_type = { (byte) 0x00, (byte) 0x01 };
      byte[] prot_type = { (byte) 0x08, (byte) 0x00 };
      byte[] hard_size = { (byte) 0x06 };
      byte[] prot_size = { (byte) 0x04 };
      byte[] op;

      public _ARP_HEADER() {
         this.ip_dstaddr = new _IP_ADDR();
         this.ip_srcaddr = new _IP_ADDR();
         this.enet_srcaddr = new _ETHERNET_ADDR();
         this.enet_dstaddr = new _ETHERNET_ADDR();
         this.op = new byte[2];
      }
   }

   _ARP_HEADER ARPRequest = new _ARP_HEADER();

   Runnable timer_3min = new Runnable() {
      public void run() {
         try {
            Thread.sleep(1000 * 60 * 3);
            del_non_enet_table();
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   };

   public void del_non_enet_table() {
      if (non_enet.length != 0 && non_enet[0].del_flag == this.del_flag) {
         if (!addr_isEquals(non_enet[0].addr, new _IP_ADDR().addr)) {
            System.out.println("TimeOut ");
            ((FileChatDlg) ((p_aUpperLayer.get(0)).GetUpperLayer(0)).GetUpperLayer(0))
                  .setChattingArea(non_enet[0].addr, null, "", 1);//
            Del_ip_addr(non_enet[0].addr, null);
         }
         if (non_enet.length > 1) {
            _IP_ADDR[] temp = new _IP_ADDR[this.non_enet.length - 1];
            for (int i = 0; i < temp.length; i++)
               temp[i] = non_enet[i + 1];
            non_enet = temp.clone();
         } else {
            non_enet = new _IP_ADDR[0];
         }
      }
      else if(non_enet.length != 0 && non_enet[0].del_flag != this.del_flag) {
         if (non_enet.length > 1) {
            _IP_ADDR[] temp = new _IP_ADDR[this.non_enet.length - 1];
            for (int i = 0; i < temp.length; i++)
               temp[i] = non_enet[i + 1];
            non_enet = temp.clone();
         } else {
            non_enet = new _IP_ADDR[0];
         }
      }
   }

   Runnable timer_20min = new Runnable() {
      public void run() {
         try {
            Thread.sleep(1000 * 60 *20);
            del_enet_table();
         } catch (InterruptedException e) {
            e.printStackTrace();
         }

      }
   };

   public boolean exist_table(byte[] ip) {
      if (table.length == 0)
         return true;
      for (int i = 0; i < table.length; i++)
         if (addr_isEquals(table[i].ip_addr.addr, ip))
            return false;
      return true;
   }

   public void del_enet_table() {
      if (enet.length != 0 && enet[0].del_flag == this.del_flag) {
         ((FileChatDlg) ((p_aUpperLayer.get(0)).GetUpperLayer(0)).GetUpperLayer(0)).setChattingArea(enet[0].addr,
               null, "", 1);
         Del_ip_addr(enet[0].addr);
         if (enet.length > 1) {
            _IP_ADDR[] temp = new _IP_ADDR[this.enet.length - 1];
            for (int i = 0; i < temp.length; i++)
               temp[i] = enet[i + 1];
            enet = temp.clone();
         } else {
            enet = new _IP_ADDR[0];
         }
      }
      else if(enet.length != 0 && enet[0].del_flag != this.del_flag) {
         if (enet.length > 1) {
            _IP_ADDR[] temp = new _IP_ADDR[this.enet.length - 1];
            for (int i = 0; i < temp.length; i++)
               temp[i] = enet[i + 1];
            enet = temp.clone();
         } else {
            enet = new _IP_ADDR[0];
         }
      }
   }

   public void set_my_ip_addr(byte[] ip_addr) {
      this.my_ip_addr.addr = ip_addr;
   }

   public void set_my_enet_addr(byte[] enet_addr) {
      this.my_enet_addr.addr = enet_addr;
   }

   public ARPLayer(String pName) throws UnknownHostException, SocketException {
      // TODO Auto-generated constructor stub
      pLayerName = pName;
      StringTokenizer st = new StringTokenizer(InetAddress.getLocalHost().getHostAddress(), ".");
      for (int i = 0; i < 4; i++)
         ARPRequest.ip_srcaddr.addr[i] = (byte) Integer.parseInt(st.nextToken());
      this.set_my_ip_addr(ARPRequest.ip_srcaddr.addr);
      InetAddress ip = InetAddress.getLocalHost();
      NetworkInterface netif = NetworkInterface.getByInetAddress(ip);
      ARPRequest.enet_srcaddr.addr = netif.getHardwareAddress();
      this.set_my_enet_addr(ARPRequest.enet_srcaddr.addr);
   }

   public boolean addr_isEquals(byte[] addr1, byte[] addr2) {
      for (int i = 0; i < addr1.length; i++) {
         if (addr1[i] - addr2[i] != 0)
            return false;
      }
      return true;
   }

   public _ETHERNET_ADDR search_table(byte[] ip_addr) {

      for (int i = 0; i < table.length; i++) {

         if (addr_isEquals(table[i].ip_addr.addr, ip_addr)) { // IP in table == received IP ?
            return table[i].mac_addr;
         }
      }
      return null;
   }

   public void Proxy_Add_ipAndMac_addr(String ip_input, byte[] mac_input) {
      StringTokenizer st = new StringTokenizer(ip_input, ".");
      byte[] ip_addr = new byte[4];

      for (int i = 0; i < 4; i++) {
         ip_addr[i] = (byte) Integer.parseInt(st.nextToken());
      }

      Array[] temp = new Array[proxyTable.length + 1];
      for (int i = 0; i < proxyTable.length; i++)
         temp[i] = proxyTable[i];
      proxyTable = temp.clone();
      proxyTable[proxyTable.length - 1] = new Array(new _IP_ADDR(ip_addr), new _ETHERNET_ADDR(mac_input));
   }

   public void Proxy_Del_ipAndMac_addr() {
      Array[] temp = new Array[0];
      proxyTable = temp.clone();
   }

   // cache table
   public void Add_ip_addr(byte[] ip_addr) {
      Array[] temp = new Array[table.length + 1];
      for (int i = 0; i < table.length; i++)
         temp[i] = table[i];
      table = temp.clone();
      table[table.length - 1] = new Array(new _IP_ADDR(ip_addr), null);

   }

   // cache table
   public void Del_ip_addr(byte[] ip_addr, byte[] enet_addr) {
      if (table.length != 0) {
         if (table.length > 1) {
            int flag = 0;
            Array[] temp = new Array[table.length - 1];
            int i, j;
            for (i = 0, j = 0; i < table.length; i++, j++) {
               if (addr_isEquals(ip_addr, table[i].ip_addr.addr)) {
                  if (addr_isEquals(enet_addr, table[i].mac_addr)) {
                     j--;
                     flag = 1;
                     continue;
                  }
               }
               temp[j] = table[i];
            }
            if (flag == 1)
               table = temp.clone();
         } else {
            table = new Array[0];
         }
      }
   }

   public void Del_ip_addr(byte[] ip_addr) {
      if (table.length != 0) {
         if (table.length > 1) {
            int flag = 0;
            Array[] temp = new Array[table.length - 1];
            int i, j;
            for (i = 0, j = 0; i < table.length; i++, j++) {
               if (addr_isEquals(ip_addr, table[i].ip_addr.addr)) {
                  j--;
                  flag = 1;
                  continue;
               }
               temp[j] = table[i];
            }
            if (flag == 0)
               table = temp.clone();
         } else {
            table = new Array[0];
         }
      }
   }

   public boolean addr_isEquals(byte[] enet_addr, _ETHERNET_ADDR enet) {
      if (enet_addr == null && enet == null)
         return true;
      if (enet_addr == null || enet == null) {
         return false;
      } else {
         if (addr_isEquals(enet_addr, enet.addr))
            return true;
      }
      return false;
   }

   // cache table
   public void Add_enet_addr(byte[] ip_addr, byte[] enet_addr) {
      if (table.length != 0) {
         for (int i = 0; i < table.length; i++)
            if (addr_isEquals(table[i].ip_addr.addr, ip_addr)) {
               table[i].mac_addr = new _ETHERNET_ADDR(enet_addr);
            }
      }
   }

   public void getNewMacAddr(byte[] macAddr) {

      this.newMacAddr = new _ETHERNET_ADDR(macAddr);
      if (newMacAddr != my_enet_addr)
         GratuitousFlag = true;

      this.Send(null, 0);
   }

   public void sendARPrequest() {

      ARPRequest.enet_srcaddr.addr = this.my_enet_addr.addr;
      ARPRequest.op[0] = (byte) 0x00;
      ARPRequest.op[1] = (byte) 0x01;
      if (!addr_isEquals(ARPRequest.ip_dstaddr.addr, ARPRequest.ip_srcaddr.addr)) {
         ((FileChatDlg) this.GetUpperLayer(0).GetUpperLayer(0).GetUpperLayer(0))
               .setChattingArea(ARPRequest.ip_dstaddr.addr, null, "incomplete", 0);// add
         Add_ip_addr(ARPRequest.ip_dstaddr.addr);
         Add_ip_non_enet_table(ARPRequest.ip_dstaddr.addr);
      }

      byte[] send = ObjToByte(ARPRequest, new byte[0], 0);

      try {
         ((EthernetLayer) this.GetUnderLayer()).SendARP(send, send.length);
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   public void Add_ip_non_enet_table(byte[] ip) {
      _IP_ADDR[] temp = new _IP_ADDR[this.non_enet.length + 1];
      for (int i = 0; i < non_enet.length; i++)
         temp[i] = non_enet[i];
      temp[temp.length - 1] = new _IP_ADDR(ip);
      temp[temp.length - 1].del_flag = this.del_flag;
      non_enet = temp.clone();
   }

   public boolean Send(byte[] input, int length) {

      // updated MAC addr
      if (GratuitousFlag == true) {

         // set my IP address as dst and scr addr
         ARPRequest.ip_dstaddr = my_ip_addr;
         ARPRequest.ip_srcaddr = my_ip_addr;
         // update my MAC addr as new MAC addr
         my_enet_addr = newMacAddr;
         GratuitousFlag = false;

      } else {

         for (int i = 0; i < 4; i++)
            ARPRequest.ip_dstaddr.addr[i] = input[16 + i];
         for (int i = 0; i < 4; i++)
            ARPRequest.ip_srcaddr.addr[i] = input[12 + i];

      }
      // search cache table --> has IP?
      if (search_table(ARPRequest.ip_dstaddr.addr) != null) {
         try {
            ((EthernetLayer) this.GetUnderLayer()).SendARP(input, input.length);
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

      } else {// cache Table has no IP --> ARP request send
         if (exist_table(ARPRequest.ip_dstaddr.addr)) {
            sendARPrequest();
            Thread thread = new Thread(timer_3min);
            thread.start();
         }
      }
      return true;
   }

   public byte[] makeARPreply(byte[] input) {

      _ARP_HEADER ARPReply = new _ARP_HEADER();
      ARPReply.op[0] = (byte) 0x00;
      ARPReply.op[1] = (byte) 0x02;
      ARPReply.ip_dstaddr = new _IP_ADDR(Arrays.copyOfRange(input, 14, 18));
      ARPReply.ip_srcaddr = new _IP_ADDR(Arrays.copyOfRange(input, 24, 28));
      ARPReply.enet_dstaddr = new _ETHERNET_ADDR(Arrays.copyOfRange(input, 8, 14));
      ARPReply.enet_srcaddr = new _ETHERNET_ADDR(this.my_enet_addr.addr);
      return ObjToByte(ARPReply, new byte[0], 0);

   }

   public void Add_ip_enet_table(byte[] ip) {
      _IP_ADDR[] temp = new _IP_ADDR[this.enet.length + 1];
      for (int i = 0; i < enet.length; i++)
         temp[i] = enet[i];
      temp[temp.length - 1] = new _IP_ADDR(ip);
      temp[temp.length - 1].del_flag = this.del_flag;
      enet = temp.clone();
   }

   public void change_ip_enet_table(byte[] ip) {
      if (enet.length != 0) {
         for (int i = 0; i < enet.length; i++) {
            if (addr_isEquals(enet[i].addr, ip)) {
               enet[i] = new _IP_ADDR();
            }
         }
         Add_ip_enet_table(ip);
      }
   }

   public void change_ip_non_to_enet_table(byte[] ip) {
      if (non_enet.length != 0) {
         for (int i = 0; i < non_enet.length; i++) {
            if (addr_isEquals(non_enet[i].addr, ip)) {
               non_enet[i] = new _IP_ADDR();
            }
         }
         Add_ip_enet_table(ip);
      }
   }

   public boolean Receive(byte[] input) {
      if (!addr_isEquals(this.my_ip_addr.addr, Arrays.copyOfRange(input, 14, 18))) {
         // ARP_request
         if (input[7] == (byte) 0x01) {

            // destination IP addr = mine
            if (addr_isEquals(this.my_ip_addr.addr, Arrays.copyOfRange(input, 24, 28))) {

               ((FileChatDlg) ((p_aUpperLayer.get(0)).GetUpperLayer(0)).GetUpperLayer(0)).setChattingArea(
                     Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14), "complete", 0);// add

               //
               Add_ip_addr(Arrays.copyOfRange(input, 14, 18));
               Add_enet_addr(Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14));

               byte[] send = makeARPreply(input);

               try {
                  ((EthernetLayer) this.GetUnderLayer()).SendARP(send, send.length);
               } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
               Add_ip_enet_table(Arrays.copyOfRange(input, 14, 18));
               Thread thread = new Thread(timer_20min);
               thread.start();

               return true;
            }
            else if (!addr_isEquals(this.my_ip_addr.addr, Arrays.copyOfRange(input, 24, 28))) {

               byte[] dstIPaddr = Arrays.copyOfRange(input, 24, 28);

               for (int i = 0; i < this.proxyTable.length; i++) { // search proxy table

                  byte[] proxyEntreeElement = proxyTable[i].ip_addr.addr;
                  if (addr_isEquals(dstIPaddr, proxyEntreeElement)) { // is it exist on proxy table?

                     byte[] send = makeARPreply(input);
                     try {
                        ((EthernetLayer) this.GetUnderLayer()).SendARP(send, send.length);
                     } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                     }

                     Add_ip_enet_table(Arrays.copyOfRange(input, 14, 18));
                     Thread thread = new Thread(timer_20min);
                     thread.start();

                     Add_ip_addr(Arrays.copyOfRange(input, 14, 18));
                     Add_enet_addr(Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14));
                     ((FileChatDlg) ((p_aUpperLayer.get(0)).GetUpperLayer(0)).GetUpperLayer(0)).setChattingArea(
                           Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14), "complete", 0);

                     return true;

                  }
               }
               if (search_table(Arrays.copyOfRange(input, 14, 18)) != null) {
                  Add_enet_addr(Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14));
                  ((FileChatDlg) ((p_aUpperLayer.get(0)).GetUpperLayer(0)).GetUpperLayer(0)).setChattingArea(
                        Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14), "complete", 2);// change
                  change_ip_enet_table(Arrays.copyOfRange(input, 14, 18));
                  Thread thread2 = new Thread(timer_20min);
                  thread2.start();
               } else {
                  Add_ip_addr(Arrays.copyOfRange(input, 14, 18));
                  Add_enet_addr(Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14));
                  ((FileChatDlg) ((p_aUpperLayer.get(0)).GetUpperLayer(0)).GetUpperLayer(0)).setChattingArea(
                        Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14), "complete", 0);// add

                  Add_ip_enet_table(Arrays.copyOfRange(input, 14, 18));
                  Thread thread2 = new Thread(timer_20min);
                  thread2.start();
               }

            }

         }
         // ARP_reply
         else if (input[7] == (byte) 0x02) {
            // source IP address is not mine
            if (!addr_isEquals(this.my_ip_addr.addr, Arrays.copyOfRange(input, 14, 18))) {

               Add_enet_addr(Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14));
               ((FileChatDlg) ((p_aUpperLayer.get(0)).GetUpperLayer(0)).GetUpperLayer(0)).setChattingArea(
                     Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14), "complete", 2);// add
               change_ip_non_to_enet_table(Arrays.copyOfRange(input, 14, 18));
               
               Thread thread = new Thread(timer_20min);
               thread.start();

            }

            // source IP address is mine
            else {
               System.out.println("!! Duplicate IP address sent from " + Arrays.copyOfRange(input, 8, 14));
            }

         }
         return false;
      }
      return false;
   }

   public void del_all() {
      this.table = new Array[0];
      del_flag++;
      // thread stop??
   }

   public byte[] ObjToByte(_ARP_HEADER ARPHeader, byte[] input, int length) {
      byte[] buf = new byte[length + 28];

      buf[0] = ARPHeader.hard_type[0];
      buf[1] = ARPHeader.hard_type[1];
      buf[2] = ARPHeader.prot_type[0];
      buf[3] = ARPHeader.prot_type[1];
      buf[4] = ARPHeader.hard_size[0];
      buf[5] = ARPHeader.prot_size[0];
      buf[6] = ARPHeader.op[0];
      buf[7] = ARPHeader.op[1];

      for (int i = 0; i < 6; i++)
         buf[8 + i] = ARPHeader.enet_srcaddr.addr[i];
      for (int i = 0; i < 4; i++)
         buf[14 + i] = ARPHeader.ip_srcaddr.addr[i];
      for (int i = 0; i < 6; i++)
         buf[18 + i] = ARPHeader.enet_dstaddr.addr[i];
      for (int i = 0; i < 4; i++)
         buf[24 + i] = ARPHeader.ip_dstaddr.addr[i];

      return buf;
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
   public void SetUnderLayer(BaseLayer pUnderLayer) {
      // TODO Auto-generated method stub
      if (pUnderLayer == null)
         return;
      p_UnderLayer = pUnderLayer;
   }

   @Override
   public void SetUpperLayer(BaseLayer pUpperLayer) {
      // TODO Auto-generated method stub
      if (pUpperLayer == null)
         return;
      this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
   }

   @Override
   public void SetUpperUnderLayer(BaseLayer pUULayer) {
      // TODO Auto-generated method stub
      this.SetUpperLayer(pUULayer);
      pUULayer.SetUnderLayer(this);
   }

}