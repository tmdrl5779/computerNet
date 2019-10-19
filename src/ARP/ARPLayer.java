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

	public _IP_ADDR() {
		for(int i = 0; i < 4; i++)
			this.addr[i] = (byte) 0x00;
	}
	
	public _IP_ADDR(byte[] ip_addr) {
		for(int i = 0; i < 4; i++)
			this.addr[i] = ip_addr[i];
	}
}

class _ETHERNET_ADDR {
	byte[] addr = new byte[6];

	public _ETHERNET_ADDR() {
		for(int i = 0; i < 6; i++)
			this.addr[i] = (byte) 0x00;
	}
	
	public _ETHERNET_ADDR(byte[] enet_addr) {
		for(int i = 0; i < 6; i++)
			this.addr[i] = enet_addr[i];
	}
}

public class ARPLayer implements BaseLayer {
	public String pLayerName = null;
	public int nUpperLayerCount = 0;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	byte[] ip_addr_temp; // for Timer
	public Array[] table = new Array[0];
	public Array[] proxyTable = new Array[0];
	
	public _IP_ADDR my_ip_addr = new _IP_ADDR(); // 0x00
	public _ETHERNET_ADDR my_enet_addr = new _ETHERNET_ADDR(); // 0x00
	public _ETHERNET_ADDR newMacAddr; // for Gratuitous ARP 
	
	byte[] flag_ARPrequest = new byte[]{0x00,0x01};
	byte[] flag_ARPreply = new byte[]{0x00,0x10};
	byte[] flag_RARPrequest = new byte[]{0x00,0x11};
	byte[] flag_RARPreply = new byte[]{0x01,0x00};
	
	public void set_my_ip_addr(byte[] ip_addr) {
		this.my_ip_addr.addr = ip_addr;
	}
	public void set_my_enet_addr(byte[] enet_addr) {
		this.my_enet_addr.addr = enet_addr;
	}
	private class _ARP_HEADER {
		
		byte[] hard_type = {(byte)0x00, (byte)0x01}; //0-1
		byte[] prot_type = {(byte)0x08, (byte)0x00}; //2-3
		byte[] hard_size = {(byte)0x06}; //4
		byte[] prot_size = {(byte)0x04}; //5
		byte[] op; //6-7
		
		_ETHERNET_ADDR enet_srcaddr; //8-13
		_IP_ADDR ip_srcaddr; //14-17
		
		_ETHERNET_ADDR enet_dstaddr; //18-23
		_IP_ADDR ip_dstaddr; //24-27
		
		public _ARP_HEADER() { //constructor
			this.ip_dstaddr = new _IP_ADDR();
			this.ip_srcaddr = new _IP_ADDR();
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.enet_dstaddr = new _ETHERNET_ADDR();
			this.op = new byte[2];
		}
	}
	_ARP_HEADER packet_ARP = new _ARP_HEADER();
	
	public ARPLayer(String pName) throws UnknownHostException, SocketException {
		
		pLayerName = pName;
		byte[] temp_addr = new byte[4];
		
		// set my MAC addr from InetAddress
		InetAddress localHost = InetAddress.getLocalHost();
		NetworkInterface nif = NetworkInterface.getByInetAddress(localHost);
		this.set_my_enet_addr(nif.getHardwareAddress());
		
		// set my IP addr from InetAddress --> set src IP of ARP_Ruquest
		StringTokenizer st = new StringTokenizer(InetAddress.getLocalHost().getHostAddress(), ".");
		for(int i = 0; i < 4; i++)
			temp_addr[i] = (byte) Integer.parseInt(st.nextToken());
		this.set_my_ip_addr(temp_addr);

	}
	
	public boolean addr_isEquals(byte[] addr1, byte[] addr2) {
		for(int i = 0; i < addr1.length; i++) {
			if(addr1[i] - addr2[i] != 0)
				return false;
		}
		return true;
	}
	
	public _ETHERNET_ADDR search_table(byte[] ip_addr) {
		
		for(int i = 0; i < table.length; i++) {
			
			if(addr_isEquals(table[i].ip_addr.addr, ip_addr)) { // IP in table == received IP ?
				return table[i].mac_addr;
			}
			
		}
		return null;
	}
	
	Runnable timer_20min = new Runnable() {
		byte[] ip_addr = ip_addr_temp;
		public void run() {
				try {
					Thread.sleep(1000*60*20);
					((FileChatDlg)((p_aUpperLayer.get(0)).GetUpperLayer(0)).GetUpperLayer(0)).setChattingArea(packet_ARP.ip_dstaddr.addr, null, "", 1);//�젣嫄�
					del_Table_IP(this.ip_addr);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

		}
	};
	Runnable timer_3min = new Runnable() {
		byte[] temp = packet_ARP.ip_dstaddr.addr;
		public void run() {
				try {
					Thread.sleep(1000*60*3);
					if(search_table(packet_ARP.ip_dstaddr.addr) == null) {
						System.out.println("TimeOut");
						((FileChatDlg)((p_aUpperLayer.get(0)).GetUpperLayer(0)).GetUpperLayer(0)).setChattingArea(temp, null, "", 1);//�젣嫄�
						del_Table_IP(temp);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	};
	
	public void add_Table_IP(byte[] ip_addr) { 
		Array[] temp = new Array[table.length + 1];
		for(int i = 0; i < table.length; i ++)
			temp[i] = table[i];
		table = temp.clone();
		table[table.length - 1] = new Array(new _IP_ADDR(ip_addr), null);
		
	}
	public void del_Table_IP(byte[] ip_addr) {
		Array[] temp = new Array[table.length - 1];
		int i,j;
		for(i = 0, j = 0; i < table.length; i++,j++) {
			if(addr_isEquals(ip_addr, table[i].ip_addr.addr)) {
				j--;
				continue;
			}
			temp[j] = table[i];
		}
		table = temp.clone();
		
	}
	public void add_Table_MAC(byte[] ip_addr, byte[] enet_addr) {
		for(int i = 0; i < table.length; i ++)
			if(addr_isEquals(table[i].ip_addr.addr, ip_addr)) {
				table[i].mac_addr = new _ETHERNET_ADDR(enet_addr);
			}
	}
	public void newMacAddr(byte[] macAddr) {
		
		this.newMacAddr = new _ETHERNET_ADDR(macAddr);
		if(newMacAddr != my_enet_addr) {
			
			// update my MAC addr as new MAC addr
			this.my_enet_addr = newMacAddr;
			
		}
		
	}

	public void sendARPpacket() {
		
		packet_ARP.op[0] = (byte) 0x00; 
		packet_ARP.op[1] = (byte) 0x01; 
		packet_ARP.ip_srcaddr.addr = this.my_ip_addr.addr;
		packet_ARP.enet_srcaddr.addr = this.my_enet_addr.addr;
		
		add_Table_IP(packet_ARP.ip_dstaddr.addr);
		
		// send packet to Ethernet Layer
		byte[] send = ObjToByte(packet_ARP, new byte[0], 0);
		try {((EthernetLayer) this.GetUnderLayer()).SendARP(send, send.length);	
		} catch (IOException e) {e.printStackTrace();}
		
		// for GUI app display
		((FileChatDlg)this.GetUpperLayer(0).GetUpperLayer(0).GetUpperLayer(0))
						.setChattingArea(packet_ARP.ip_dstaddr.addr, null, "incomplete", 0);
		
	}
	public boolean Send(byte[] packetFromIP_layer, int length){

		// parse packet from IP layer
		byte[] dstIPfromApp = new byte[4];
		byte[] srcIPfromIPlayer = new byte[4];
		for (int i = 0; i < 4; i++)
			dstIPfromApp[i] = packetFromIP_layer[16 + i];
		for (int i = 0; i < 4; i++)
			srcIPfromIPlayer[i] = packetFromIP_layer[12 + i];
		
		// set ARP request with dst IP
		packet_ARP.ip_dstaddr.addr = dstIPfromApp;
			
		// search cache table --> has MAC --> display to App
		if (search_table(packet_ARP.ip_dstaddr.addr) != null) {
						
			try {((EthernetLayer) this.GetUnderLayer())
									.SendARP(packetFromIP_layer, packetFromIP_layer.length);
			} catch (IOException e) { e.printStackTrace();}
		
		} else {// cache Table has no MAC --> ARP request send
			
			sendARPpacket();
			Thread thread = new Thread(timer_3min);
			thread.start();
			
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

	public boolean Receive(byte[] packetfromEtherLayer) {

		// parse packet
		byte[] srcMACaddr = Arrays.copyOfRange(packetfromEtherLayer, 8, 14);
		byte[] srcIPaddr = Arrays.copyOfRange(packetfromEtherLayer, 14, 18);
		byte[] dstIPaddr = Arrays.copyOfRange(packetfromEtherLayer, 24, 28);
		byte[] opCode = Arrays.copyOfRange(packetfromEtherLayer, 6, 8);

		if (opCode == flag_ARPrequest) {	
			
			if (addr_isEquals(this.my_ip_addr.addr, dstIPaddr)) {

				((FileChatDlg)((p_aUpperLayer.get(0)).GetUpperLayer(0)).GetUpperLayer(0))
				.setChattingArea(srcIPaddr, srcMACaddr, "complete", 0);//add

				add_Table_IP(srcIPaddr);
				add_Table_MAC(srcIPaddr, srcMACaddr);

				// send ARP packet to Ethernet Layer
				byte[] send = makeARPreply(packetfromEtherLayer);	
				try { ((EthernetLayer) this.GetUnderLayer()).SendARP(send, send.length);
				} catch (IOException e) { e.printStackTrace(); }

				ip_addr_temp = srcIPaddr;
				Thread thread = new Thread(timer_20min);
				thread.start();

				return true;
			}
			else if (!addr_isEquals(this.my_ip_addr.addr, dstIPaddr)) {

				// search cache table --> src MAC addr changed ?		
				if (search_table(dstIPaddr).addr != srcMACaddr) {
						
					// update new MAC addr to table
					add_Table_MAC(dstIPaddr, srcMACaddr);
					
					Thread thread = new Thread(timer_20min);
					thread.start();

				}
				// search proxy table
				for (int i = 0; i < this.proxyTable.length; i++) { 

					byte[] proxyEntreeElement = proxyTable[i].ip_addr.addr;
					
					// is it exist on proxy table?
					if (addr_isEquals(dstIPaddr, proxyEntreeElement)) { 

						byte[] send = makeARPreply(packetfromEtherLayer);
						try { ((EthernetLayer) this.GetUnderLayer()).SendARP(send, send.length);
						} catch (IOException e) { e.printStackTrace(); }

		                ip_addr_temp = srcIPaddr;
		                
						Thread thread = new Thread(timer_20min);
						thread.start();

						return true;

					}
				}

			}
			
			ip_addr_temp = srcIPaddr;
			add_Table_IP(srcIPaddr);
			add_Table_MAC(srcIPaddr, srcMACaddr);
			Thread thread = new Thread(timer_20min);
			thread.start();

			((FileChatDlg)((p_aUpperLayer.get(0)).GetUpperLayer(0)).GetUpperLayer(0)).
							setChattingArea(srcIPaddr, srcMACaddr, "complete", 0);

		}
		else if (opCode == flag_ARPreply) {
			
			// source IP address is not mine
			if (!addr_isEquals(this.my_ip_addr.addr, srcIPaddr)){

				add_Table_MAC(srcIPaddr, srcMACaddr);
				((FileChatDlg)((p_aUpperLayer.get(0)).GetUpperLayer(0)).GetUpperLayer(0))
										.setChattingArea(srcIPaddr, srcMACaddr, "complete", 2);

							
				ip_addr_temp = srcIPaddr;
				Thread thread = new Thread(timer_20min);
				thread.start();

			}
			// source IP address is mine
			else { System.out.println("!! Duplicate IP address sent from " + srcMACaddr); }

		}
		return false;
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
		
		for(int i = 0; i < 6; i++)
			buf[8 + i] = ARPHeader.enet_srcaddr.addr[i];
		for(int i = 0; i < 4; i++)
			buf[14 + i] = ARPHeader.ip_srcaddr.addr[i];
		for(int i = 0; i < 6; i++)
			buf[18 + i] = ARPHeader.enet_dstaddr.addr[i];
		for(int i = 0; i < 4; i++)
			buf[24 + i] = ARPHeader.ip_dstaddr.addr[i];
		
		return buf;
	}
	
	public void Proxy_Add_ipAndMac_addr(String ip_input, byte[] mac_input) {
		StringTokenizer st = new StringTokenizer(ip_input, ".");
		byte[] ip_addr = new byte[4];
		
		for (int i = 0; i < 4; i++) {
			ip_addr[i] = (byte) Integer.parseInt(st.nextToken());
		}
		
		Array[] temp = new Array[proxyTable.length + 1];
		for(int i = 0; i < proxyTable.length; i ++)
			temp[i] = proxyTable[i];
		proxyTable = temp.clone();
		proxyTable[proxyTable.length - 1] = new Array(new _IP_ADDR(ip_addr), new _ETHERNET_ADDR(mac_input));
	}
	
	public void Proxy_Del_ipAndMac_addr(byte[] ip_addr, byte[] enet_addr) {
		Array[] temp = new Array[proxyTable.length - 1];
		int i,j;
		for(i = 0, j = 0; i < proxyTable.length; i++,j++) {
			if(addr_isEquals(ip_addr, proxyTable[i].ip_addr.addr) && addr_isEquals(enet_addr, proxyTable[i].mac_addr.addr)) {
				j--;
				continue;
			}
			temp[j] = proxyTable[i];
		}
		proxyTable = temp.clone();
		//ip�젣嫄� 異쒕젰
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
