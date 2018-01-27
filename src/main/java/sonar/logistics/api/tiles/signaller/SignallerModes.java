package sonar.logistics.api.tiles.signaller;

import java.util.List;
import java.util.function.Function;

import sonar.logistics.api.utils.Result;

public enum SignallerModes {
	ONE_TRUE, ONE_FALSE, ALL_TRUE, ALL_FALSE;

	/** SUCCESS = the Redstone Signaller should emit a signal, regardless of any remaining statements PASS = this statement passes through the mode, but others need to be checked before SUCCESS FAIL = the Redstone Signaller should not emit a signal, regardless of any remaining statements */
	public Result isStillValid(boolean toValidate, boolean isLast) {
		switch (this) {
		case ALL_FALSE:
			return !toValidate ? (isLast ? Result.SUCCESS : Result.PASS) : Result.FAIL;
		case ALL_TRUE:
			return toValidate ? (isLast ? Result.SUCCESS : Result.PASS) : Result.FAIL;
		case ONE_FALSE:
			return !toValidate ? Result.SUCCESS : (isLast ? Result.FAIL : Result.PASS);
		case ONE_TRUE:
			return toValidate ? Result.SUCCESS : (isLast ? Result.FAIL : Result.PASS);
		}
		return Result.FAIL;
	}

	public <T> boolean checkList(List<T> list, Function<T, Boolean> func) {
		Result result = Result.FAIL;
		int position = 0;
		statements: for (T s : list) {
			position++;
			boolean matching = func.apply(s);
			Result sResult = isStillValid(matching, position == list.size());
			switch (sResult) {
			case PASS:
				continue statements;
			default:
				result = sResult;
				break statements;
			}
		}
		return result.getBoolean();
	}
}
