package ARP;

import java.util.ArrayList;
import java.util.Arrays;

class Addrs {// ip�� �̴��� �ּҸ� ���� �� ��ü
	_IP_ADDR ip_addr;
	_ETHERNET_ADDR mac_addr;

	public Addrs(_IP_ADDR ip_addr, _ETHERNET_ADDR mac_addr) {
		this.ip_addr = ip_addr;
		this.mac_addr = mac_addr;
	}
}

class _IP_ADDR {// ip�ּ� ���� ��ü
	byte[] addr = new byte[4];

	public _IP_ADDR() {
		for (int i = 0; i < 4; i++)
			this.addr[i] = (byte) 0x00;
	}

	public _IP_ADDR(byte[] ip_addr) {
		for (int i = 0; i < 4; i++)
			this.addr[i] = ip_addr[i];
	}
}

class _ETHERNET_ADDR {// �̴��� �ּ� ���� ��ü
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
	public Addrs[] table = new Addrs[0];
	public Addrs[] proxyTable = new Addrs[0]; // proxyTable
	public _IP_ADDR my_ip_addr = new _IP_ADDR();
	public _ETHERNET_ADDR my_enet_addr = new _ETHERNET_ADDR();
	private Boolean gratuitousFlag = false;

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
			this.enet_dstaddr = new _ETHERNET_ADDR();
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.op = new byte[2];
		}

	}

	Runnable timer_3min = new Runnable() {
		public void run() {
			try {
				Thread.sleep(1000 * 60 * 3);
				if (search_table(ARPRequest.ip_dstaddr.addr) == null) {// 3���� ���� �������� �������� �̴��� �ּҰ� �߰� �� �Ǿ����� ���
					System.out.println("TimeOut");
//                  ((FileChatDlg)p_aUpperLayer.get(2)).setChattingArea(ARPRequest.ip_dstaddr.addr, null, "", 1);//����
					Del_ip_addr(ARPRequest.ip_dstaddr.addr);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	};

	byte[] ip_addr_temp;

	Runnable timer_20min = new Runnable() {
		byte[] ip_addr = ip_addr_temp;

		public void run() {
			try {
				Thread.sleep(1000 * 60 * 20);
//               ((FileChatDlg)p_aUpperLayer.get(2)).setChattingArea(ARPRequest.ip_dstaddr.addr, null, "", 1);//����
				Del_ip_addr(this.ip_addr);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	};

	_ARP_HEADER ARPRequest = new _ARP_HEADER();

	public void set_my_ip_addr(byte[] ip_addr) {
		this.my_ip_addr.addr = ip_addr;
	}

	public void set_my_enet_addr(byte[] enet_addr) {
		this.my_enet_addr.addr = enet_addr;
	}

	public ARPLayer(String pName) {
		// TODO Auto-generated constructor stub
		pLayerName = pName;

	}

	public boolean addr_isEquals(byte[] addr1, byte[] addr2) {// �ּҰ��� ��
		for (int i = 0; i < addr1.length; i++) {
			if (addr1[i] - addr2[i] != 0)
				return false;
		}
		return true;
	}

	public _ETHERNET_ADDR search_table(byte[] ip_addr) {// ���̺� ����� ������ �ּ����� Ȯ��
		for (int i = 0; i < table.length; i++)
			if (addr_isEquals(table[i].ip_addr.addr, ip_addr)) {
				return table[i].mac_addr;
			}

		return null;
	}

	public void Proxy_Add_ipAndMac_addr(byte[] ip_addr, byte[] enet_addr) {// ���Ͻ� ���̺� ������ �ּҿ� ���ּҸ� ����
		Addrs[] temp = new Addrs[proxyTable.length + 1];
		for (int i = 0; i < proxyTable.length; i++)
			temp[i] = proxyTable[i];
		proxyTable = temp.clone();
		proxyTable[proxyTable.length - 1] = new Addrs(new _IP_ADDR(ip_addr), new _ETHERNET_ADDR(enet_addr));
	}

	public void Proxy_Del_ipAndMac_addr(byte[] ip_addr, byte[] enet_addr) {// ���Ͻ� ���̺� �ε��� ����
		Addrs[] temp = new Addrs[proxyTable.length - 1];
		int i, j;
		for (i = 0, j = 0; i < proxyTable.length; i++, j++) {
			if (addr_isEquals(ip_addr, proxyTable[i].ip_addr.addr)
					&& addr_isEquals(enet_addr, proxyTable[i].mac_addr.addr)) {
				j--;
				continue;
			}
			temp[j] = proxyTable[i];
		}
		proxyTable = temp.clone();
		// ip���� ���
	}

	public void Add_ip_addr(byte[] ip_addr) {// ���̺� ������ �ּҸ� ����
		Addrs[] temp = new Addrs[table.length + 1];
		for (int i = 0; i < table.length; i++)
			temp[i] = table[i];
		table = temp.clone();
		table[table.length - 1] = new Addrs(new _IP_ADDR(ip_addr), null);
		// ??���
	}

	public void Del_ip_addr(byte[] ip_addr) {// ���̺� ������ �ּҸ� ����
		Addrs[] temp = new Addrs[table.length - 1];
		int i, j;
		for (i = 0, j = 0; i < table.length; i++, j++) {
			if (addr_isEquals(ip_addr, table[i].ip_addr.addr)) {
				j--;
				continue;
			}
			temp[j] = table[i];
		}
		table = temp.clone();
		// ip���� ���
	}

	public void Add_enet_addr(byte[] ip_addr, byte[] enet_addr) {// ���̺��� ������ �ּҿ� �ش��ϴ� �̴��� �ּҸ� ����
		for (int i = 0; i < table.length; i++)
			if (addr_isEquals(table[i].ip_addr.addr, ip_addr)) {
				table[i].mac_addr = new _ETHERNET_ADDR(enet_addr);
			}
	}

	public boolean Send(byte[] input, int length) {

		for (int i = 0; i < 4; i++)
			ARPRequest.ip_srcaddr.addr[i] = input[12 + i];
		for (int i = 0; i < 4; i++)
			ARPRequest.ip_dstaddr.addr[i] = input[16 + i];

		if (search_table(ARPRequest.ip_dstaddr.addr) != null) {// ���̺� �����ǰ� ����Ǿ� �ִ� ���
			((EthernetLayer) this.GetUnderLayer()).Send(input, input.length);
		} else {// ���̺� ����Ǿ����� ���� ���
			Add_ip_addr(ARPRequest.ip_dstaddr.addr);// ������ �ּҸ� ���̺� ������
			ARPRequest.enet_srcaddr.addr = this.my_enet_addr.addr;
			ARPRequest.op[0] = (byte) 0x00;
			ARPRequest.op[1] = (byte) 0x01;// ARP�� ��û���� �����ϰ�
//         ((FileChatDlg)this.GetUpperLayer(2)).setChattingArea(ARPRequest.ip_dstaddr.addr, null, "incomplete", 0);//add
			// ip��� �̴��� ?????
			byte[] send = ObjToByte(ARPRequest, new byte[0], 0);// ARP�� ����Ʈ�� �ٲپ� send�迭�� ����

			((EthernetLayer) this.GetUnderLayer()).Send(send, send.length);
			Thread thread = new Thread(timer_3min);
			thread.start();
		}
		return true;
	}

	public void GratuitousARP_MAC(_ETHERNET_ADDR newMacAddr) {

		// updated MAC addr
		if (newMacAddr != my_enet_addr) {

			// set my IP address as dstaddr
			ARPRequest.ip_dstaddr = my_ip_addr;
			ARPRequest.enet_srcaddr = newMacAddr;

			// send ARP request
//         ((EthernetLayer)this.GetUnderLayer()).Send(ARPRequest, ARPRequest.length);

		}

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

	public boolean Receive(byte[] input) {

		// ARP_request
		if (input[7] == 1) {

			// destination IP addr = mine
			if (addr_isEquals(this.my_ip_addr.addr, Arrays.copyOfRange(input, 24, 28))) {

//            ((FileChatDlg)this.GetUpperLayer(2)).setChattingArea(Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14), "complete", 0);//add

				// ���??
				Add_ip_addr(Arrays.copyOfRange(input, 14, 18));
				Add_enet_addr(Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14));

				byte[] send = makeARPreply(input);
				((EthernetLayer) this.GetUnderLayer()).Send(send, send.length);

				ip_addr_temp = Arrays.copyOfRange(input, 14, 18);
				Thread thread = new Thread(timer_20min);
				thread.start();

				return true;
			}
			// destination IP addr != mine
			else if (!addr_isEquals(this.my_ip_addr.addr, Arrays.copyOfRange(input, 24, 28))) {

				// MAC addr changed
				if (!addr_isEquals(this.my_enet_addr.addr, Arrays.copyOfRange(input, 8, 14))) {

					// update table

				}

				byte[] dstIPaddr = Arrays.copyOfRange(input, 24, 28);
				for (int i = 0; i < this.proxyTable.length; i++) { // search proxy table

					byte[] proxyEntreeElement = proxyTable[i].ip_addr.addr;
					if (addr_isEquals(dstIPaddr, proxyEntreeElement)) { // ���Ͻ� ��Ʈ���� ��û�� IP�� �����ϸ�

						byte[] send = makeARPreply(input);
						((EthernetLayer) this.GetUnderLayer()).Send(send, send.length);

//                  ip_addr_temp = Arrays.copyOfRange(input, 14, 18);
						Thread thread = new Thread(timer_20min);
						thread.start();

						return true;

					}
				}

			}

			// ����� ip�ּҿ� mac�ּҸ� ���̺� ������
			Add_ip_addr(Arrays.copyOfRange(input, 14, 18));
			Add_enet_addr(Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14));
			((FileChatDlg) this.GetUpperLayer(2)).setChattingArea(Arrays.copyOfRange(input, 14, 18),
					Arrays.copyOfRange(input, 8, 14), "complete", 0);// add

			// ip �̴��� ���
			ip_addr_temp = Arrays.copyOfRange(input, 14, 18);
			Thread thread = new Thread(timer_20min);
			thread.start();

		}
		// ARP_reply
		else if (input[7] == 2) {

			// destination IP address is mine
			if (addr_isEquals(this.my_ip_addr.addr, Arrays.copyOfRange(input, 24, 28))) {
				System.out.println("!! Duplicate IP address sent from " + Arrays.copyOfRange(input, 8, 14));
			}

			// destination IP address is not mine
			else {

				Add_enet_addr(Arrays.copyOfRange(input, 14, 18), Arrays.copyOfRange(input, 8, 14));
				((FileChatDlg) this.GetUpperLayer(2)).setChattingArea(Arrays.copyOfRange(input, 14, 18),
						Arrays.copyOfRange(input, 8, 14), "complete", 2);// add

				// ip �̴��� ���
				ip_addr_temp = Arrays.copyOfRange(input, 14, 18);
				Thread thread = new Thread(timer_20min);
				thread.start();

			}
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
		if (p_UnderLayer == null)
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
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		// TODO Auto-generated method stub
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}

}
