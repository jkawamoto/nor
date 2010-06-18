package nor.core.proxy.filter;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ReadonlyPatternFilterAdapter extends ReadonlyStringFilterAdapter{

	private final Pattern pat;

	//============================================================================
	//  コンストラクタ
	//============================================================================
	public ReadonlyPatternFilterAdapter(final Pattern pat){

		this.pat = pat;

	}

	public ReadonlyPatternFilterAdapter(final String regex){

		this.pat = Pattern.compile(regex);

	}

	//============================================================================
	//  Public methods
	//============================================================================
	@Override
	public final void update(final String in) {

		final Matcher m = this.pat.matcher(in);
		while(m.find()){

			this.update(m.toMatchResult());

		}

	}

	public abstract void update(final MatchResult res);

}
