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
package nor.http.server.proxyserver;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.util.Querable;
import nor.util.log.Logger;

/**
 * 外部プロキシ用のルーティングテーブル．
 * 正規表現を用いたURLパターン毎に外部プロキシを設定することができます．
 *
 * @author Junpei Kawamoto
 *
 */
public class Router implements Map<Pattern, Proxy>, Querable<Proxy>{

	private final Map<Pattern, Proxy> routs = new HashMap<Pattern, Proxy>();

	private static final Pattern AddressPat = Pattern.compile("([^:]+):(\\d+)");
	private static final Logger LOGGER = Logger.getLogger(Router.class);

	@Override
	public Proxy query(final String url){
		LOGGER.entering("query", url);
		assert url != null;

		Proxy res = Proxy.NO_PROXY;
		for(final Pattern p : this.routs.keySet()){

			final Matcher m = p.matcher(url);
			if(m.find()){

				res = this.routs.get(p);

			}
		}

		LOGGER.exiting("query", res);
		return res;
	}

	public void put(final Pattern pat, final InetSocketAddress addr){
		LOGGER.entering("put", pat, addr);
		assert pat != null;
		assert addr != null;

		this.put(pat, new Proxy(Type.HTTP, addr));

		LOGGER.exiting("put");
	}

	public void put(final String regex, final InetSocketAddress addr){
		this.put(Pattern.compile(regex), addr);
	}

	public void put(final Pattern pat, final String url){
		LOGGER.entering("pur", pat, url);

		final Matcher m = AddressPat.matcher(url);
		if(m.matches()){

			final String host = m.group(1);
			final int port = Integer.valueOf(m.group(2));

			this.put(pat, host, port);

		}else{

			LOGGER.warning("put", "Invalid parameter: {0}", url);

		}

		LOGGER.exiting("put");
	}

	public void put(final String regex, final String url){
		this.put(Pattern.compile(regex), url);
	}

	public void put(final String regex, final String host, final int port){
		this.put(Pattern.compile(regex), host, port);
	}

	public void put(final Pattern pat, final String host, final int port){
		LOGGER.entering("pur", pat, host, port);

		this.put(pat, new InetSocketAddress(host, port));

		LOGGER.exiting("put");
	}

	//--------------------------------------------------------------------
	//  委譲メソッド
	//--------------------------------------------------------------------
	/* (非 Javadoc)
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear() {

		this.routs.clear();

	}

	/* (非 Javadoc)
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object key) {

		return routs.containsKey(key);

	}

	/* (非 Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value) {

		return routs.containsValue(value);

	}

	/* (非 Javadoc)
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set<java.util.Map.Entry<Pattern, Proxy>> entrySet() {

		return routs.entrySet();

	}

	/* (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		return routs.equals(o);

	}

	/* (非 Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public Proxy get(Object key) {

		return routs.get(key);

	}

	/* (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return routs.hashCode();

	}

	/* (非 Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty() {

		return routs.isEmpty();

	}

	/* (非 Javadoc)
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set<Pattern> keySet() {

		return routs.keySet();

	}

	/* (非 Javadoc)
	 * @see java.util.Map#put(K, V)
	 */
	@Override
	public Proxy put(Pattern key, Proxy value) {

		return routs.put(key, value);

	}

	/* (非 Javadoc)
	 * @see java.util.Map#putAll(java.util.Map<? extends K,? extends V>)
	 */
	@Override
	public void putAll(Map<? extends Pattern, ? extends Proxy> m) {

		routs.putAll(m);

	}

	/* (非 Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public Proxy remove(Object key) {

		return routs.remove(key);

	}

	/* (非 Javadoc)
	 * @see java.util.Map#size()
	 */
	@Override
	public int size() {

		return routs.size();

	}

	/* (非 Javadoc)
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection<Proxy> values() {

		return routs.values();

	}


}
