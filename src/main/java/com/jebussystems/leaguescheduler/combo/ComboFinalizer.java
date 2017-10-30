package com.jebussystems.leaguescheduler.combo;

import java.io.IOException;
import java.util.List;

public interface ComboFinalizer<T> {
	void finalize(List<T> values) throws IOException;
}
