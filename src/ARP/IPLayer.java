package ARP;

import java.util.ArrayList;
import java.util.StringTokenizer;

import ARP.BaseLayer;

public class IPLayer implements BaseLayer{
	public int nUpperLayerCount = 0;
	public int nUnderLayerCount = 0;
	public String pLayerName = null;
	//public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_UnderLayer = new ArrayList<BaseLayer>();
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	
	private class _IP_HEADER {
		_IP_ADDR ip_dstaddr; //ip address of source
		_IP_ADDR ip_srcaddr; //ip address of destination
		
		byte[] ip_verlen; //ip version
		byte[] ip_tos; //type of servce
		byte[] ip_len; //total packet length
		byte[] ip_id; //datagram id
		byte[] ip_flagoff; //fragment offset
		byte[] ip_ttl; //time to live, in gateway hop
		byte[] ip_proto; //ip protocol
		byte[] ip_cksum; //header checksum		
		byte[] ip_data; //variable length data
		
		public _IP_HEADER() {
			this.ip_dstaddr = new _IP_ADDR();
			this.ip_srcaddr = new _IP_ADDR();
			
			this.ip_verlen = new byte[1];
			this.ip_tos = new byte[1];
			this.ip_len = new byte[2];
			this.ip_id = new byte[2];
			this.ip_flagoff = new byte[2];
			this.ip_ttl = new byte[1];
			this.ip_proto = new byte[1];
			this.ip_cksum = new byte[2];
			this.ip_data = null;
		}
	}
	
	private class _IP_ADDR {
		private byte[] addr = new byte[4];

		private _IP_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
		}
	}
	
	
	_IP_HEADER m_sHeader = new _IP_HEADER();
	
	public IPLayer(String pName) {
		
		pLayerName = pName;
		ResetHeader();
		
	}
	
	public void ResetHeader() {
		
		//reset ip version, dstaddr, srcaddr
		for (int i = 0; i < 4; i++) {
			m_sHeader.ip_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.ip_srcaddr.addr[i] = (byte) 0x00;
		}
		
		m_sHeader.ip_verlen[0] = (byte) 0x04;//IPv4
	}



	public boolean Send(byte[] input, int length) {
		
		byte[] send = ObjToByte(m_sHeader, input, length);
		((ARPLayer)this.GetUnderLayer(1)).Send(send, send.length);
		return true;
	}
	
	public byte[] ObjToByte(_IP_HEADER Header, byte[] input, int length) {
	
		byte[] buf = new byte[length + 20];

		buf[0] = Header.ip_verlen[0];
		buf[2] = Header.ip_len[0];
		buf[3] = Header.ip_len[1];
		buf[4] = Header.ip_id[0];
		buf[5] = Header.ip_id[1];
		buf[6] = Header.ip_flagoff[0];
		buf[7] = Header.ip_flagoff[1];
		buf[8] = Header.ip_ttl[0];
		buf[9] = Header.ip_proto[0];
		buf[10] = Header.ip_cksum[0];
		buf[11] = Header.ip_cksum[1];
		
		for (int i = 0; i < 4; i++)
			buf[12 + i] = Header.ip_srcaddr.addr[i];
		
		for (int i = 0; i < 4; i++)
			buf[16 + i] = Header.ip_dstaddr.addr[i];

		for (int i = 0; i < length; i++)
			buf[20 + i] = input[i];

		return buf;
	}

	public void SetIpDstAddress(String address) {
		StringTokenizer st = new StringTokenizer(address, ".");
		
		for(int i = 0; i < 4; i++)
			m_sHeader.ip_dstaddr.addr[i] = (byte) Integer.parseInt(st.nextToken());
	}
	
	public void SetIpSrcAddress(String address) {
		StringTokenizer st = new StringTokenizer(address, ".");
		
		for(int i = 0; i < 4; i++)
			m_sHeader.ip_srcaddr.addr[i] = (byte) Integer.parseInt(st.nextToken());
	}
	
	
	public synchronized boolean Receive(byte[] input) {

		byte[] data = RemoveIPHeader(input, input.length);
		((TCPLayer)this.GetUpperLayer(0)).Receive(data);
		return true;
	}

	public byte[] RemoveIPHeader(byte[] input, int length) {
		byte[] buf = new byte[length - 20];

		for (int i = 20; i < length; i++)
			buf[i-20] = input[i];

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
		return p_UnderLayer.get(0);
	}
	
	public BaseLayer GetUnderLayer(int i) {
		// TODO Auto-generated method stub
		if (i < 0 || i > nUnderLayerCount || nUnderLayerCount < 0)
			return null;
		return this.p_UnderLayer.get(i);
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
//		if (pUnderLayer == null)
//			return;
//		p_UnderLayer = pUnderLayer;
//		
		if (pUnderLayer == null)
			return;
		
		this.p_UnderLayer.add(nUnderLayerCount++, pUnderLayer);
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
