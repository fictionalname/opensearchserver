/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.collapse;

import java.io.IOException;
import java.io.Serializable;
import java.util.BitSet;

import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultScoreDoc;

public abstract class Collapse<T extends Result<?>> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int[] collapseCount;
	private int collapsedDocCount;
	protected BitSet collapsedSet;
	transient protected T result;
	private int collapseMax;

	protected Collapse(T result) throws IOException {
		this.setResult(result);
		this.collapseCount = null;
		this.collapsedDocCount = 0;
		this.collapseMax = result.getRequest().getCollapseMax();
	}

	public void setResult(T result) throws IOException {
		this.result = result;
	}

	public ResultScoreDoc[] run() throws IOException {

		prepare();

		ResultScoreDoc[] fetchedDoc = result.getFetchedDocs();
		if (fetchedDoc == null)
			return null;
		collapsedSet = new BitSet(fetchedDoc.length);

		String lastTerm = null;
		int adjacent = 0;
		collapsedDocCount = 0;
		for (int i = 0; i < fetchedDoc.length; i++) {
			String term = getTerm(fetchedDoc[i]);
			if (term != null && term.equals(lastTerm)) {
				if (++adjacent >= collapseMax)
					collapsedSet.set(i);
			} else {
				lastTerm = term;
				adjacent = 0;
			}
		}
		collapsedDocCount = collapsedSet.cardinality();
		return reduce();
	}

	protected abstract void prepare() throws IOException;

	/**
	 * Returns then term by the position
	 * 
	 * @param pos
	 * @return
	 * @throws IOException
	 */
	protected abstract String getTerm(ResultScoreDoc doc) throws IOException;

	protected abstract ResultScoreDoc[] reduce();

	public int getCount(int pos) {
		if (this.collapseCount == null)
			return 0;
		return this.collapseCount[pos];
	}

	public BitSet getBitSet() {
		return this.collapsedSet;
	}

	public int getDocCount() {
		return this.collapsedDocCount;
	}

}
