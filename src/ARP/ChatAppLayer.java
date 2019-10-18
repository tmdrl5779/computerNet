package ARP;

import java.util.ArrayList;

public class ChatAppLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _CAPP_APP {

		byte[] capp_totlen;
		byte capp_type;
		byte cpp_unused;
		byte[] capp_data;

		public _CAPP_APP() {
			this.capp_totlen = new byte[2];
			this.capp_type = 0x00;
			this.cpp_unused = 0x00;
			this.capp_data = null;
		}
	}

	_CAPP_APP m_sHeader = new _CAPP_APP();
	
	byte[] buf;
	int bufIndex = 0;
	
	public ChatAppLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		for (int i = 0; i < 2; i++) {
			m_sHeader.capp_totlen[i] = (byte) 0x00;
		}
		m_sHeader.capp_data = null;
	}
	
	public byte[] ObjToByteNoFrac(_CAPP_APP Header, byte[] input, int length) {
		byte[] buf = new byte[length + 4];
		buf[0] = (byte) (length % 256);
		buf[1] = (byte) (length / 256);
		buf[2] = 0x00;
		buf[3] = m_sHeader.cpp_unused;

		for (int i = 0; i < length; i++)
			buf[4 + i] = input[i];

		return buf;
	}
	
	public byte[] ObjToByte(_CAPP_APP Header, byte[] input, int length) {
		byte[] buf = new byte[length + 4];
		buf[0] = (byte) (length % 256);
		buf[1] = (byte) (length / 256);
		buf[2] = 0x02;
		buf[3] = m_sHeader.cpp_unused;

		for (int i = 0; i < length; i++)
			buf[4 + i] = input[i];

		return buf;
	}
	
	public byte[] ObjToByteFirst(_CAPP_APP Header, byte[] input, int length) {
		byte[] buf = new byte[length + 4];
		buf[0] = (byte) (length % 256);
		buf[1] = (byte) (length / 256);
		buf[2] = 0x01;
		buf[3] = m_sHeader.cpp_unused;

		for (int i = 0; i < length; i++)
			buf[4 + i] = input[i];

		return buf;
	}
	
	public byte[] ObjToByteLast(_CAPP_APP Header, byte[] input, int length) {
		byte[] buf = new byte[length + 4];
		buf[0] = (byte) (length % 256);
		buf[1] = (byte) (length / 256);
		buf[2] = 0x03;
		buf[3] = m_sHeader.cpp_unused;

		for (int i = 0; i < length; i++)
			buf[4 + i] = input[i];

		return buf;
	}

//	public boolean Send(byte[] input, int length) {
//		int frameNumber = 0;
//		if (length % 10 == 0) frameNumber = length / 10;
//		else frameNumber = length / 10 + 1;
//		int frameIndex = 0; // 1씩증가할것
//		
//		byte[] temp = new byte[10];
//		
//		if (length <= 10) {
//			byte[] dt = ObjToByteNoFrac(m_sHeader, input, length);
//			this.GetUnderLayer().Send(dt, length + 4);
//			return true;
//		}
//		
//		// ack받음
//		if (length > 10) {
//			for (int i = 0; i < frameNumber; i++) {
//				if ((i == frameNumber - 1) && (length % 10 != 0)) {
//					byte[] temp2 = new byte[length % 10];
//					byte[] copy = new byte[length % 10];
//					System.arraycopy(input, 10 * frameIndex, copy, 0, length % 10);
//					System.arraycopy(copy, 0, temp2, 0, length % 10);
//					byte[] bytes = ObjToByteLast(m_sHeader, temp2, length % 10);
//					this.GetUnderLayer().Send(bytes, length % 10 + 4);
//					return true;
// 				}
//				
//			byte[] copy = new byte[10];
//			System.arraycopy(input, 10 * frameIndex, copy, 0, 10);
//			System.arraycopy(copy, 0, temp, 0, 10);
//			
//			if (i == 0) {
//				byte[] bytes = ObjToByteFirst(m_sHeader, temp, 10);
//				this.GetUnderLayer().Send(bytes, 14);
//			}
//			else if (i < frameNumber - 1) {
//				byte[] bytes = ObjToByte(m_sHeader, temp, 10);
//				this.GetUnderLayer().Send(bytes, 14);
//			}
//			else {
//				byte[] bytes = ObjToByteLast(m_sHeader, temp, 10);
//				this.GetUnderLayer().Send(bytes, 14);
//			}
//			frameIndex++;
//			}
//			return true;
//		}
//		
//		byte[] bytes = ObjToByteLast(m_sHeader, input, length);
//		this.GetUnderLayer().Send(bytes, length + 4);
//
//		return true;
//	}
	
	public boolean Send(byte[] input, int length) {
		int frameNumber = 0;
		if (length % 1456 == 0) frameNumber = length / 1456;
		else frameNumber = length / 1456 + 1;
		int frameIndex = 0; // 1씩증가할것
		
		byte[] temp = new byte[1456];
		
		if (length <= 1456) {
			byte[] dt = ObjToByteNoFrac(m_sHeader, input, length);
			this.GetUnderLayer().Send(dt, length + 4);
			return true;
		}
		
		// ack받음
		if (length > 1456) {
			for (int i = 0; i < frameNumber; i++) {
				if ((i == frameNumber - 1) && (length % 1456 != 0)) {
					byte[] temp2 = new byte[length % 1456];
					byte[] copy = new byte[length % 1456];
					System.arraycopy(input, 1456 * frameIndex, copy, 0, length % 1456);
					System.arraycopy(copy, 0, temp2, 0, length % 1456);
					byte[] bytes = ObjToByteLast(m_sHeader, temp2, length % 1456);
					this.GetUnderLayer().Send(bytes, length % 1456 + 4);
					return true;
 				}
				
			byte[] copy = new byte[1456];
			System.arraycopy(input, 1456 * frameIndex, copy, 0, 1456);
			System.arraycopy(copy, 0, temp, 0, 1456);
			
			if (i == 0) {
				byte[] bytes = ObjToByteFirst(m_sHeader, temp, 1456);
				this.GetUnderLayer().Send(bytes, 1456 + 4);
			}
			else if (i < frameNumber - 1) {
				byte[] bytes = ObjToByte(m_sHeader, temp, 1456);
				this.GetUnderLayer().Send(bytes, 1456 + 4);
			}
			else {
				byte[] bytes = ObjToByteLast(m_sHeader, temp, 1456);
				this.GetUnderLayer().Send(bytes, 1456 + 4);
			}
			frameIndex++;
			}
			return true;
		}
		
		byte[] bytes = ObjToByteLast(m_sHeader, input, length);
		this.GetUnderLayer().Send(bytes, length + 4);

		return true;
	}
	
	public byte[] RemoveCappHeader(byte[] input, int length) {

		byte[] remove = new byte[length - 4];
		System.arraycopy(input, 4, remove, 0, length - 4);
		return remove;
	}
	
	public synchronized boolean Receive(byte[] input) {
		byte[] data;
		data = RemoveCappHeader(input, input.length);
		
		if (input[2] == (byte)0x00) {
			this.GetUpperLayer(0).Receive(data);
		}
		
		if (input[2] == (byte)0x01) {
			buf = new byte[data.length];
			for (int i = 0; i < data.length; i++) {
				buf[i] = data[i];
				this.bufIndex++;
			}
		}
		if (input[2] == (byte)0x02) {
			byte[] temp = new byte[buf.length + data.length];
			System.arraycopy(buf, 0, temp, 0, buf.length);
			buf = temp;
			
			for (int i = 0; i < data.length; i++) {
				buf[this.bufIndex] = data[i];
				this.bufIndex++;
			}
		}
		if ((input[2] == (byte)0x03)) {
			byte[] temp = new byte[buf.length + data.length];
			System.arraycopy(buf, 0, temp, 0, buf.length);
			buf = temp;
			
			for (int i = 0; i < data.length; i++) {
				buf[this.bufIndex] = data[i];
				this.bufIndex++;
			}
			
			this.GetUpperLayer(0).Receive(buf);
			byte[] tmp = new byte[1456];
			this.buf = tmp;
			bufIndex = 0;
		}
		return true;
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
