package ARP;

import java.util.ArrayList;

public class TCPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	public TCPLayer(String pName) {
		// super(pName);
		pLayerName = pName;
	}

	private class _TCP_HEADER {
		byte[] tcp_sport = new byte[2]; // source port
		byte[] tcp_dport = new byte[2]; // destination port
		byte[] tcp_seq = new byte[4]; // sequence number
		byte[] tcp_ack = new byte[4]; // acknowledged sequence
		byte[] tcp_offset = new byte[1]; // no use
		byte[] tcp_flag = new byte[1]; // control flag
		byte[] tcp_window = new byte[2]; // no use
		byte[] tcp_cksum = new byte[2]; // check sum
		byte[] tcp_urgptr = new byte[2]; // no use
		// byte[] Padding;
		// byte[] tcp_data; // data size

		public _TCP_HEADER() { // header 초기화

			this.tcp_sport[0] = (byte) 0x20; // source port
			this.tcp_sport[1] = (byte) 0x19;
			this.tcp_dport[0] = (byte) 0x20; // destination port
			this.tcp_dport[1] = (byte) 0x19;

			this.tcp_seq[0] = (byte) 0x00; // sequence number
			this.tcp_seq[1] = (byte) 0x00;
			this.tcp_seq[2] = (byte) 0x00;
			this.tcp_seq[3] = (byte) 0x00;

			this.tcp_ack[0] = (byte) 0x00; // acknowledged sequence
			this.tcp_ack[1] = (byte) 0x00;
			this.tcp_ack[2] = (byte) 0x00;
			this.tcp_ack[3] = (byte) 0x00;

			this.tcp_offset[0] = (byte) 0x00; // no use
			this.tcp_flag[0] = (byte) 0x00; // control flag

			this.tcp_window[0] = (byte) 0x00; // no use
			this.tcp_window[1] = (byte) 0x00;

			this.tcp_cksum[0] = (byte) 0x00; // check sum
			this.tcp_cksum[1] = (byte) 0x00;

			this.tcp_urgptr[0] = (byte) 0x00; // no use
			this.tcp_urgptr[1] = (byte) 0x00;
			// this.Padding = new byte[4]; //??
			// this.tcp_data = new byte[TCP_DATA_SIZE]; // data size

		}
	}

	/*
	 * private class IP { private byte[] srcAddr = new byte[4]; private byte[]
	 * dstAddr = new byte[4];
	 * 
	 * public IP() { this.srcAddr[0] = (byte) 0x00; this.srcAddr[1] = (byte) 0x00;
	 * this.srcAddr[2] = (byte) 0x00; this.srcAddr[3] = (byte) 0x00;
	 * 
	 * this.dstAddr[0] = (byte) 0x00; this.dstAddr[1] = (byte) 0x00; this.dstAddr[2]
	 * = (byte) 0x00; this.dstAddr[3] = (byte) 0x00; } }
	 */

	_TCP_HEADER tcp_Header = new _TCP_HEADER();
	// IP ip = new IP();

	String srcIpAddress;
	String dstIpAddress;

	public byte[] ObjToByte(_TCP_HEADER Header, byte[] input, int length) { // TCP 헤더 붙이기

		byte[] buf = new byte[length + 20];

		// 헤더 지정
		// buf[0 ~ 19]
		buf[0] = Header.tcp_sport[0];
		buf[1] = Header.tcp_sport[1];

		buf[2] = Header.tcp_dport[0];
		buf[3] = Header.tcp_dport[1];

		buf[4] = Header.tcp_seq[0];
		buf[5] = Header.tcp_seq[1];
		buf[6] = Header.tcp_seq[2];
		buf[7] = Header.tcp_seq[3];

		buf[8] = Header.tcp_ack[0];
		buf[9] = Header.tcp_ack[1];
		buf[10] = Header.tcp_ack[2];
		buf[11] = Header.tcp_ack[3];

		buf[12] = Header.tcp_offset[0];
		buf[13] = Header.tcp_flag[0];

		buf[14] = Header.tcp_window[0];
		buf[15] = Header.tcp_window[1];

		buf[16] = Header.tcp_cksum[0];
		buf[17] = Header.tcp_cksum[1];

		buf[18] = Header.tcp_urgptr[0];
		buf[19] = Header.tcp_urgptr[1];

		for (int i = 0; i < length; i++)
			buf[20 + i] = input[i];

		return buf;
	}

	public void SetIpSrcAddress(String srcIpAddress) { // Gui에서 설정한 ip
		// ip.srcAddr = StringToIntArray(srcIpAddress); // 자신의 ip설정 (4바이트)
		this.srcIpAddress = srcIpAddress; // Gui 에서 string형으로 ip를 받는다.
	}

	public void SetIpDstAddress(String dstIpAddress) {// Gui에서 설정한 ip
		// ip.dstAddr = StringToIntArray(dstIpAddress); //목적지 ip 설정 (4바이트)
		this.dstIpAddress = dstIpAddress; // Gui 에서 string형으로 ip를 받는다.
	}

	public boolean Send(byte[] input, int length) { // data send
		byte[] bytes = ObjToByte(tcp_Header, input, length);
		((IPLayer) this.GetUnderLayer()).SetIpSrcAddress(srcIpAddress);
		((IPLayer) this.GetUnderLayer()).SetIpDstAddress(dstIpAddress);
		this.GetUnderLayer().Send(bytes, length + 20); // 하위 레이어의 send() 메소드에 내린다.
		return true;
	}

	public byte[] RemoveCappHeader(byte[] input, int length) { // header 제거

		byte[] header = new byte[length - 20];
		for (int i = 20; i < length; i++) {
			header[i - 20] = input[i];
		} // header없앤 14 byte를 없앤 새로운 배열 선언

		return header;
	}

	public synchronized boolean Receive(byte[] input) { // receive
		byte[] data;
		byte[] temp_src = tcp_Header.tcp_sport;
		byte[] temp_dst = tcp_Header.tcp_dport;

		for (int i = 0; i < 2; i++) {
			if (input[i] != temp_dst[i] || input[i + 2] != temp_src[i]) { // 포트번호 안맞을경우 discard
				return false;
			}
		}

		data = RemoveCappHeader(input, input.length); // header 제거
		this.GetUpperLayer(0).Receive(data);
		// 주소설정
		return true;
	}

	/*
	 * public static byte[] StringToIntArray(String s) { //String -> byte
	 * 
	 * String[] ip = s.split("\\."); byte[] data = new byte[ip.length]; for(int i =
	 * 0; i < data.length; i++) { data[i] = (byte)Integer.parseInt(ip[i]); } return
	 * data; }
	 */

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
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}

}
