/*
 *  Copyright (C) 2010 Junpei Kawamoto
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nor.core.proxy.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 登録したパターンを探しながら転送するフィルタ．
 *
 * @author Junpei Kawamoto
 * @since 0.1.20100629
 *
 */
public class ReadonlyPatternMatchingFilter extends ReadonlyStringFilterAdapter{

	private final Map<Pattern, List<MatchingEventListener>> listeners = new HashMap<Pattern, List<MatchingEventListener>>();

	//============================================================================
	//  Public methods
	//============================================================================
	/* (非 Javadoc)
	 * @see nor.core.proxy.filter.ReadonlyStringFilter#update(java.lang.String)
	 */
	@Override
	public final void update(final String in) {

		for(final Pattern pat : this.listeners.keySet()){

			final Matcher m = pat.matcher(in);
			while(m.find()){

				for(final MatchingEventListener l : this.listeners.get(pat)){

					l.update(m.toMatchResult());

				}

			}

		}

	}

	/**
	 * パターンを指定してマッチイベントのリスナを登録する．
	 * pat にマッチするパターンが発見された場合，l で指定したリスナにイベントが通知されます．
	 *
	 * @param pat 登録するパターン．
	 * @param listener 登録するイベントリスナ
	 */
	public void addEventListener(final Pattern pat, final MatchingEventListener listener){

		if(this.listeners.containsKey(pat)){

			this.listeners.get(pat).add(listener);

		}else{

			final List<MatchingEventListener> list = new ArrayList<MatchingEventListener>();
			list.add(listener);
			this.listeners.put(pat, list);

		}

	}

	/**
	 * パターンを指定してマッチイベントのリスナを登録する．
	 * regex にマッチするパターンが発見された場合，l で指定したリスナにイベントが通知されます．
	 *
	 * @param regex 登録する正規表現文字列．
	 * @param listener 登録するイベントリスナ
	 */
	public void addEventListener(final String regex, final MatchingEventListener listener){

		this.addEventListener(Pattern.compile(regex), listener);

	}

	/**
	 * 登録済みのリスナを削除する．
	 *
	 * @param l 削除するイベントリスナ
	 */
	public void removeEventListener(final MatchingEventListener l){

		for(final Pattern pat : this.listeners.keySet()){

			this.listeners.get(pat).remove(l);

		}

	}

	/**
	 * パターンマッチングイベントのリスナが実装すべきインタフェース．
	 *
	 * @author Junpei Kawamoto
	 *
	 */
	public interface MatchingEventListener{

		/**
		 * パターンにマッチする文字列が見つかったことを通知する．
		 *
		 * @param result パターンマッチングの結果
		 */
		public void update(final MatchResult result);

	}

}
