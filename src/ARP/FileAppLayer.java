package ARP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.sound.midi.Sequence;

public class FileAppLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	public String fileName = "";
	public String finalFileName;
	public int fileSize = 0; // ��̸���Ʈ �ε�����
	public ArrayList<byte[]> fracBuf = new ArrayList<byte[]>();
	public int totalLength = 0;
	
	public float file_status;
	
	private class _FAPP_HEADER {
		byte[] fapp_totlen;
		byte[] fapp_type;
		byte fapp_msg_type;
		byte ed;
		byte[] fapp_seq_num;
		byte[] fapp_data;

		public _FAPP_HEADER() {
			this.fapp_totlen = new byte[4];
			this.fapp_type = new byte[2];
			this.fapp_msg_type = 0x00;
			this.ed = 0x00;
			this.fapp_seq_num = new byte[4];
			this.fapp_data = null;
		}
	}

	_FAPP_HEADER m_sHeader = new _FAPP_HEADER();
	
	byte[] buf;
	int bufIndex = 0;
	
	public FileAppLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
//		for (int i = 0; i < 2; i++) {
//			m_sHeader.capp_totlen[i] = (byte) 0x00;
//		}
//		m_sHeader.capp_data = null;
		for (int i = 0; i < 4; i++) {
			m_sHeader.fapp_totlen[i] = (byte)0x00;
		}
		m_sHeader.fapp_data = null;
	}
	
	public byte[] intToByte4(int value) {
		byte[] temp = new byte[4];
    
		temp[0] |= (byte) ((value & 0xFF000000) >> 24);
		temp[1] |= (byte) ((value & 0xFF0000) >> 16);
		temp[2] |= (byte) ((value & 0xFF00) >> 8);
		temp[3] |= (byte) (value & 0xFF);

		return temp;
	}
	
	 public static int byte4ToInt(byte[] src) {
	      int s1 = src[0] & 0xFF;
	      int s2 = src[1] & 0xFF;
	      int s3 = src[2] & 0xFF;
	      int s4 = src[3] & 0xFF;

	      return ((s1 << 24) + (s2 << 16) + (s3 << 8) + (s4 << 0));
	  }
	
	//����ȭ x (0x00)
	public byte[] ObjToByteNoFrac(_FAPP_HEADER header, byte[] input, int length) {
		byte[] temp = this.intToByte4(length); // ���� length
		byte[] buf = new byte[length + 12]; 
		
		for (int i = 0; i < 4; i++) {
			buf[i] = temp[i];
		}
		
		buf[4] = 0x00; // type
		buf[5] = 0x00; // type
		buf[6] = 0x00; // ���ϸ� or ����
		buf[7] = 0x00; // unused
		buf[8] = 0x00;
		buf[9] = 0x00;
		buf[10] = 0x00;
		buf[11] = 0x00;
		
		for (int i = 0; i < length; i++)
			buf[12 + i] = input[i];

		return buf;
	}
	
	//���ϸ� ������
	public byte[] ObjToByteFileName(_FAPP_HEADER header, byte[] input, int length) {
		byte[] temp = this.intToByte4(length); // ���� length
		byte[] buf = new byte[length + 12]; 
		
		for (int i = 0; i < 4; i++) {
			buf[i] = temp[i];
		}
		
		buf[4] = 0x00; // type
		buf[5] = 0x00; // type
		buf[6] = 0x01; // ���ϸ� or ���� ���ϸ��ϰ�� 0x01
		buf[7] = 0x00; // unused
		buf[8] = 0x00;
		buf[9] = 0x00;
		buf[10] = 0x00;
		buf[11] = 0x00;
		
		for (int i = 0; i < length; i++)
			buf[12 + i] = input[i];

		return buf;
	}
	
	//�߰� (0x02)
	public byte[] ObjToByte(_FAPP_HEADER Header, byte[] input, int length, int sequence) {
		byte[] temp = this.intToByte4(length); // ���� length
		byte[] fileSequence = this.intToByte4(sequence); //���� sequence
		byte[] buf = new byte[length + 12]; 
		
		//length
		for (int i = 0; i < 4; i++) {
			buf[i] = temp[i];
		}
		
		buf[4] = 0x01; //type
		buf[5] = 0x00; //type
		buf[6] = 0x00; //���ϸ� or ����
		buf[7] = 0x00; //unused
		buf[8] = 0x00;
		buf[9] = 0x00;
		buf[10] = 0x00;
		buf[11] = 0x00;
		
		//sequence
		for (int i = 8; i < 12; i++) {
			buf[i] = fileSequence[i - 8];
		}
		
		for (int i = 0; i < length; i++)
			buf[12 + i] = input[i];

		return buf;
	}
	
	//ù��° ����ȭ sequence�� ������ 1
	public byte[] ObjToByteFirst(_FAPP_HEADER Header, byte[] input, int length) {
		byte[] temp = this.intToByte4(length); // ���� length
//		byte[] fileSequence = this.intToByte4(1); // sequence�� 1
		byte[] fileSequence = this.intToByte4(0);
		byte[] buf = new byte[length + 12]; 
		
		//length
		for (int i = 0; i < 4; i++) {
			buf[i] = temp[i];
		}
		
		buf[4] = 0x01; //type
		buf[5] = 0x00; //type
		buf[6] = 0x00; //���ϸ� or ����
		buf[7] = 0x00; //unused
		buf[8] = 0x00;
		buf[9] = 0x00;
		buf[10] = 0x00;
		buf[11] = 0x00;
		
		//sequence
		for (int i = 8; i < 12; i++) {
			buf[i] = fileSequence[i - 8];
		}
		
		for (int i = 0; i < length; i++)
			buf[12 + i] = input[i];

		return buf;
	}
	
	//������ ����ȭ sequence��??
	//������ ����ȭ �ٺ����� �������� 0�����Ұ�
	public byte[] ObjToByteLast(_FAPP_HEADER Header, byte[] input, int length, int sequence) {
		byte[] temp = this.intToByte4(length); // ���� length
		byte[] fileSequence = this.intToByte4(sequence); //���� sequence
		byte[] buf = new byte[length + 12]; 
		
		//length
		for (int i = 0; i < 4; i++) {
			buf[i] = temp[i];
		}
		
		buf[4] = 0x02; //type
		buf[5] = 0x00; //type
		buf[6] = 0x00; //���ϸ� or ����
		buf[7] = 0x00; //unused
		buf[8] = 0x00;
		buf[9] = 0x00;
		buf[10] = 0x00;
		buf[11] = 0x00;
		
		//sequence
		for (int i = 8; i < 12; i++) {
			buf[i] = fileSequence[i - 8];
		}
		
		for (int i = 0; i < length; i++)
			buf[12 + i] = input[i];

		return buf;
	}
	
	public boolean Send(byte[] input, int length, String name) {
		int frameNumber = 0;
		int frameIndex = 0;
		byte[] temp = new byte[1448];
		this.fileName = name; // ���ϳ��� �̰Ÿ� ����Ʈ�� �ٲ����
		byte[] fileNameByte = this.fileName.getBytes();
		//this.buf = new byte[length];
		
		if (length % 1448 == 0) 
			frameNumber = length / 1448; // 1456���� ����������� 
		else 
			frameNumber = length / 1448 + 1; // �ȳ����������
		
		this.fileSize = frameNumber;
		
		if (length <= 1448) {
			byte[] sendName = ObjToByteFileName(m_sHeader, fileNameByte, fileNameByte.length);
			this.GetUnderLayer().SendFile(sendName, fileNameByte.length + 12);
			byte[] dt = ObjToByteNoFrac(m_sHeader, input, length);
			this.GetUnderLayer().SendFile(dt, length + 12);
			FileChatDlg chat_file = (FileChatDlg) this.GetUpperLayer(0);
	        chat_file.underProgressBar.setMaximum(frameNumber);
	        chat_file.underProgressBar.setValue(frameNumber);
			return true;
		}
		
		if (length > 1448) {
			for (int i = 0; i < frameNumber; i++) {
				
				if ((i == frameNumber - 1) && (length % 1448 != 0)) {
					byte[] temp2 = new byte[length % 1448];
					byte[] copy = new byte[length % 1448];
					System.arraycopy(input, 1448 * frameIndex, copy, 0, length % 1448);
					System.arraycopy(copy, 0, temp2, 0, length % 1448);
					byte[] bytes = ObjToByteLast(m_sHeader, temp2, length % 1448, frameIndex);
					try {
						Thread.sleep(2);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					this.GetUnderLayer().SendFile(bytes, length % 1448 + 12);
					System.out.println("�������۽� ����ȭ ��ȣ�� : " + frameIndex);
					this.setStatusOfFile(frameNumber, frameIndex);
					return true;
				}
			
		byte[] copy = new byte[1448];
		System.arraycopy(input, 1448 * frameIndex, copy, 0, 1448);
		System.arraycopy(copy, 0, temp, 0, 1448);
		
		if (i == 0) {
			byte[] sendName = ObjToByteFileName(m_sHeader, fileNameByte, fileNameByte.length);
			this.GetUnderLayer().SendFile(sendName, fileNameByte.length + 12);
			System.out.println();
			byte[] bytes = ObjToByteFirst(m_sHeader, temp, 1448);
//			this.GetUnderLayer().SendFile(bytes, 14);
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.GetUnderLayer().SendFile(bytes, 1448 + 12);
			frameIndex++;
			this.setStatusOfFile(frameNumber, frameIndex);
		} 
		else if (i < frameNumber - 1) {
			byte[] bytes = ObjToByte(m_sHeader, temp, 1448, frameIndex);
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.GetUnderLayer().SendFile(bytes, 1448 + 12);
			frameIndex++;
			System.out.println("�۽� ����ȭ ��ȣ : " + frameIndex);
		
			this.setStatusOfFile(frameNumber, frameIndex);
		}
		else {
			byte[] bytes = ObjToByteLast(m_sHeader, temp, 1448, frameIndex);
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.GetUnderLayer().SendFile(bytes, 1448 + 12);
			System.out.println("�������۽� ����ȭ ��ȣ�� : " + frameIndex);
		}
//		frameIndex++;
		FileChatDlg chat_file = (FileChatDlg) this.GetUpperLayer(0);
        chat_file.underProgressBar.setMaximum(frameNumber);
        chat_file.underProgressBar.setValue(i + 2);
		}
		
		return true;
	}
		
		byte[] bytes = ObjToByteLast(m_sHeader, input, length, frameIndex);
		this.GetUnderLayer().SendFile(bytes, length + 12);

		return true;
	
	}
	
	public byte[] fileToArray(String path) throws IOException {
		Path pathOfFile =  FileSystems.getDefault().getPath(path);
		byte[] buf = Files.readAllBytes(pathOfFile);
		System.out.println(path);
		return buf;
	}
	
	public byte[] RemoveFileHeader(byte[] input, int length) {

		byte[] remove = new byte[length - 12];
		System.arraycopy(input, 12, remove, 0, length - 12);
		return remove;
	}
	
	public synchronized boolean Receive(byte[] input) {
		byte[] data;
		data = RemoveFileHeader(input, input.length);
		
		if (input[4] == (byte)0x00) {
			if (input[6] == (byte)0x01) {
				String fn = new String(data).trim();
				System.out.println(fn + "��");
				this.finalFileName = fn; // ��¥ ������ ���ϸ�

				byte[] sizeOfFile = new byte[4];
				for (int i = 0; i < 4; i++) {
					sizeOfFile[i] = input[i];
				}
				
				return true;
			}
			this.buf = data;
			this.writeToFile(finalFileName, data);
			return true;
		}
		
		if (input[4] == (byte)0x01) {
				this.totalLength += input.length;
				this.fracBuf.add(input);
				System.out.println("oooooo");
				this.bufIndex++;
		}
		if (input[4] == (byte)0x02) {
				this.totalLength += input.length;
				this.fracBuf.add(input);
				this.bufIndex++;
				this.buf = new byte[this.totalLength];

				for (int i = 0; i < this.fracBuf.size(); i++) {
				byte[] temp = fracBuf.get(i);
				byte[] sequenceByte = new byte[4];
				
				for (int j = 8; j < 12; j++) {
					sequenceByte[j - 8] = temp[j];
				}
				
				
				byte[] data2;
				data2 = RemoveFileHeader(temp, temp.length);
				int sequence = this.byte4ToInt(sequenceByte);
				System.arraycopy(data2, 0, this.buf, 1448 * (sequence), data2.length);
				
			}
			this.writeToFile(finalFileName, this.buf);
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
	
	public void writeToFile(String filename, byte[] pData){ //byte���� ���Ϸ� ����� 
	       if(pData == null){
	           return;
	       }
	       int lByteArraySize = pData.length;
	       System.out.println(filename);
	       try{
	           File lOutFile = new File("./"+filename);
	           FileOutputStream lFileOutputStream = new FileOutputStream(lOutFile);
	           lFileOutputStream.write(pData);
	           lFileOutputStream.close();
	       }catch(Throwable e){
	           e.printStackTrace(System.out);
	       }

	   }
	public void setStatusOfFile(int big, int small) {
		this.file_status = big / small;
	}
	
	public float getStatusOfFile() {
		return this.file_status * 100;
	}
}
