package com.jebussystems.leaguescheduler.combo;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class ComboUtils {
	public static <T> void combinations(int index, int length, List<Collection<T>> list, List<T> combo,
			ComboFinalizer<T> finalizer) throws IOException {
		if (index == length) {
			// complete combination found
			finalizer.finalize(combo);
			return;
		}
		// go through all values
		Collection<T> values = list.get(index);
		for (T value : values) {
			// set the value
			combo.add(value);
			// recurse for the next team
			combinations(index + 1, length, list, combo, finalizer);
			combo.remove(index);
		}
	}
}
