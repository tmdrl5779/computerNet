package ARP;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {
   public String pLayerName = null;
   public int nUpperLayerCount = 0;
   public BaseLayer p_UnderLayer = null;
   public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

   public EthernetLayer(String pName) {
      // super(pName);
      pLayerName = pName;
   }

   private class _ETHERNET_ADDR {
      private byte[] addr = new byte[6];

      public _ETHERNET_ADDR() {
         this.addr[0] = (byte) 0x00;
         this.addr[1] = (byte) 0x00;
         this.addr[2] = (byte) 0x00;
         this.addr[3] = (byte) 0x00;
         this.addr[4] = (byte) 0x00;
         this.addr[5] = (byte) 0x00;
      }
   }

   private class _ETHERNET_Frame {
      _ETHERNET_ADDR enet_dstaddr;
      _ETHERNET_ADDR enet_srcaddr;
      byte[] enet_type;
      byte[] enet_data;

      public _ETHERNET_Frame() {
         this.enet_dstaddr = new _ETHERNET_ADDR();
         this.enet_srcaddr = new _ETHERNET_ADDR();
         this.enet_type = new byte[2];
         this.enet_data = null;
      }
   }

   _ETHERNET_Frame ethernetHeader = new _ETHERNET_Frame();

   public byte[] ObjToByte(_ETHERNET_Frame Header, byte[] input, int length) {
      byte[] buf = new byte[length + 14];
      byte[] srctemp = Header.enet_srcaddr.addr;
      byte[] dsttemp = Header.enet_dstaddr.addr;

      buf[0] = dsttemp[0];
      buf[1] = dsttemp[1];
      buf[2] = dsttemp[2];
      buf[3] = dsttemp[3];
      buf[4] = dsttemp[4];
      buf[5] = dsttemp[5];
      buf[6] = srctemp[0];
      buf[7] = srctemp[1];
      buf[8] = srctemp[2];
      buf[9] = srctemp[3];
      buf[10] = srctemp[4];
      buf[11] = srctemp[5];
      buf[12] = (byte)0x20;
      buf[13] = (byte)0x80;

      for (int i = 0; i < length; i++)
         buf[14 + i] = input[i];

      return buf;
   }
   
   public byte[] ObjToByte_ARP(_ETHERNET_Frame Header, byte[] input, int length) {
      byte[] buf = new byte[length + 14];
      byte[] temp = new byte[6];
      System.arraycopy(input, 8, temp, 0, 6);
      this.SetEnetSrcAddress(temp);
      
      byte[] srctemp = Header.enet_srcaddr.addr;
      byte[] dsttemp = Header.enet_dstaddr.addr;
      
      if (input[6] == (byte)0x00 && input[7] == (byte)0x02) { // ARP reply
         buf[0] = dsttemp[0];
         buf[1] = dsttemp[1];
         buf[2] = dsttemp[2];
         buf[3] = dsttemp[3];
         buf[4] = dsttemp[4];
         buf[5] = dsttemp[5];
         buf[6] = srctemp[0];
         buf[7] = srctemp[1];
         buf[8] = srctemp[2];
         buf[9] = srctemp[3];
         buf[10] = srctemp[4];
         buf[11] = srctemp[5];
         buf[12] = (byte)0x08;
         buf[13] = (byte)0x06;
      }
      else {
         buf[0] = (byte)0xff;
         buf[1] = (byte)0xff;
         buf[2] = (byte)0xff;
         buf[3] = (byte)0xff;
         buf[4] = (byte)0xff;
         buf[5] = (byte)0xff;
         buf[6] = srctemp[0];
         buf[7] = srctemp[1];
         buf[8] = srctemp[2];
         buf[9] = srctemp[3];
         buf[10] = srctemp[4];
         buf[11] = srctemp[5];
         buf[12] = (byte)0x08;
         buf[13] = (byte)0x06;
      }
      
      for (int i = 0; i < length; i++)
         buf[14 + i] = input[i];

      return buf;
   }
   
   public byte[] ObjToByte_File(_ETHERNET_Frame Header, byte[] input, int length) {
      byte[] buf = new byte[length + 14];
      byte[] srctemp = Header.enet_srcaddr.addr;
      byte[] dsttemp = Header.enet_dstaddr.addr;

      buf[0] = dsttemp[0];
      buf[1] = dsttemp[1];
      buf[2] = dsttemp[2];
      buf[3] = dsttemp[3];
      buf[4] = dsttemp[4];
      buf[5] = dsttemp[5];
      buf[6] = srctemp[0];
      buf[7] = srctemp[1];
      buf[8] = srctemp[2];
      buf[9] = srctemp[3];
      buf[10] = srctemp[4];
      buf[11] = srctemp[5];
      buf[12] = (byte)0x20;
      buf[13] = (byte)0x90;

      for (int i = 0; i < length; i++)
         buf[14 + i] = input[i];

      return buf;
   }

   public boolean Send(byte[] input, int length) {
      for(int i = 0; i < this.ethernetHeader.enet_srcaddr.addr.length; i++) {
         System.out.print(this.ethernetHeader.enet_srcaddr.addr[i]+" ");
      }
      byte[] bytes = ObjToByte(ethernetHeader, input, length);
      this.GetUnderLayer().Send(bytes, length + 14);
      return true;
   }
   
   public boolean SendFile(byte[] input, int length) {
      byte[] bytes = ObjToByte_File(ethernetHeader, input, length);
      this.GetUnderLayer().Send(bytes, length + 14);
      return true;
   }
   
   public boolean SendARP(byte[] input, int length) throws IOException{
      byte[] bytes = ObjToByte_ARP(ethernetHeader, input, length);
//      ((NILayer)this.GetUnderLayer()).SetAdapterNumber();
      this.GetUnderLayer().Send(bytes, length + 14);
      return true;
   }

   public boolean Receive(byte[] input) {
//      if ((input[12] != (byte) 0x20) || (input[13] != (byte) 0x80)) {
//         if ((input[12]) != (byte)0x20 || (input[13]) != (byte)0x90)
//            return false;
//      }
      System.out.println("ether receive");
      
      byte[] data;
      byte[] temp_src = this.ethernetHeader.enet_srcaddr.addr;
      byte[] temp_dst = this.ethernetHeader.enet_dstaddr.addr;
      int broadCastCount = 0;
      byte[] temp;   
      temp = hexStringToByteArray("FF");
      
//      for (int i = 0; i < 6; i++) {
//         if (input[i] != temp_src[i] || input[i + 6] != temp_dst[i]) {
//            if (input[i] == temp[0]) { // broadcast
// 
//               broadCastCount++;
//               if (broadCastCount == i + 1)
//                  continue;
//            }
//
//            return false;
//         }
//      }
      // ����
//      if ((input[12]) == (byte)0x20 && (input[13]) == (byte)0x90) {
//         data = RemoveEtherHeader(input, input.length);
//         this.GetUpperLayer(1).Receive(data);
//         return true;
//      }
      if (this.isItMyPacket(input))
         return false;
      
      // ������ ��� Ÿ���� ARP�� ARPLayer�� �ø�
      if (this.isItARP(input)) {
         data = RemoveEtherHeader(input, input.length);
         this.GetUpperLayer(1).Receive(data);
         return true;
      }
      
      // ������ ��� Ÿ���� IP�� IPLayer�� �ø�
//      if (this.isItIP(input)) {
//         data = RemoveEtherHeader(input, input.length);
//         this.GetUpperLayer(0).Receive(data);
//         return true;
//      }
      
//      data = RemoveEtherHeader(input, input.length);
//      this.GetUpperLayer(1).Receive(data);
      return false;
   }
   
   // ������ ��� Ÿ�԰˻��Ͽ� ARP���� Ȯ��
   public boolean isItARP(byte[] head) {
      if ((head[12]) == (byte)0x08 && (head[13]) == (byte)0x06)
         return true;
      else
         return false;
   }
   
   // ������ ��� Ÿ�԰˻��Ͽ� IP���� Ȯ��
   public boolean isItIP(byte[] head) {
      if ((head[12]) == (byte)0x08 && (head[13]) == (byte)0x00)
         return true;
      else
         return false;
   }
   
   // dst�� src�� ������ ���� ��Ŷ
   public boolean isItMyPacket(byte[] head) {
     try {
        int count = 0;
         
         InetAddress ip = InetAddress.getLocalHost();
        byte[] myMac = null;
               
        // 네트워크 인터페이스 취득
        NetworkInterface netif = NetworkInterface.getByInetAddress(ip);
         myMac = netif.getHardwareAddress();
        
         for (int i = 0; i <= 5; i++) {
            if (myMac[i] == head[i + 6])
               count++;
         }
         
         if(count == 6)
            return true;
         else
            return false;
      
   } catch (Exception e) {
      // TODO: handle exception
       return true;
   }
     
   }
   
   public byte[] RemoveEtherHeader(byte[] input, int length) {
      byte[] data = new byte[length - 14];
      for (int i = 0; i < length - 14; i++) 
         data[i] = input[14 + i];
      return data;
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

   public void SetEnetSrcAddress(byte[] srcAddress) {
      // TODO Auto-generated method stub
      ethernetHeader.enet_srcaddr.addr = srcAddress;
   }

   public void SetEnetDstAddress(byte[] dstAddress) {
      // TODO Auto-generated method stub
      ethernetHeader.enet_dstaddr.addr = dstAddress;
   }

   public static String byteArrayToHexString(byte[] bytes) {

      StringBuilder sb = new StringBuilder();

      for (byte b : bytes) {
         sb.append(String.format("%02X", b & 0xff));
      }

      return sb.toString();
   }

   public static byte[] hexStringToByteArray(String s) {
      int len = s.length();
      byte[] data = new byte[len / 2];
      for (int i = 0; i < len; i += 2) {
         data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
      }
      return data;
   }
   
}