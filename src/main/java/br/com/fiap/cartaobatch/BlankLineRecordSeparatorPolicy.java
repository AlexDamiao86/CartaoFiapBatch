package br.com.fiap.cartaobatch;

import org.springframework.batch.item.file.separator.RecordSeparatorPolicy;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;

public class BlankLineRecordSeparatorPolicy extends SimpleRecordSeparatorPolicy 
											implements RecordSeparatorPolicy {

	@Override
	public boolean isEndOfRecord(final String line) {
		return line.trim().length() != 0 && super.isEndOfRecord(line);
	}

	@Override
	public String postProcess(final String line) {
        if (line == null || line.trim().length() == 0) {
            return null;
        }
        return super.postProcess(line);
	}

}
