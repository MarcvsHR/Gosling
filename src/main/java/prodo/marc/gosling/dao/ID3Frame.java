package prodo.marc.gosling.dao;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ID3Frame extends ID3Size {
    private String frameID;
    private byte flag1;
    private byte flag2;
    private byte encoding;
    private byte[] content;
    private byte[] MCDI_header;

    public ID3Frame(String header, String data) {
        super();
        frameID = header;
        flag1 = 0;
        flag2 = 0;
        encoding = 0;
        content = data.getBytes();
        setSize(data.length()+1,false);
    }

    public ID3Frame() {    }

}