/*
 * J-USE - Java prototyping for the UML based specification environment (USE)
 * Copyright (C) 2012 Fernando Brito e Abrey, QUASAR research group
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.quasar.use2java.types;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * A parameterized hashset whose values can be garbage collected.
 */
public class WeakHashSet<T> implements Set<T>
{

	public static class HashableWeakReference<T> extends WeakReference<T>
	{
		public int	hashCode;

		public HashableWeakReference(T referent, ReferenceQueue<T> queue)
		{
			super(referent, queue);
			this.hashCode = referent.hashCode();
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(Object obj)
		{
			if (!(obj instanceof HashableWeakReference<?>))
				return false;
			T referent = get();
			T other = ((HashableWeakReference<T>) obj).get();
			if (referent == null)
				return other == null;
			return referent.equals(other);
		}

		@Override
		public int hashCode()
		{
			return this.hashCode;
		}

		@Override
		public String toString()
		{
			T referent = get();
			if (referent == null)
				return "[hashCode=" + this.hashCode + "] <referent was garbage collected>";
			return "[hashCode=" + this.hashCode + "] " + referent.toString();
		}
	}

	HashableWeakReference<T>[]	values;
	public int					elementSize;								// number of elements in the table
	int							threshold;
	ReferenceQueue<T>			referenceQueue	= new ReferenceQueue<T>();

	public WeakHashSet()
	{
		this(5);
	}

	@SuppressWarnings("unchecked")
	public WeakHashSet(int size)
	{
		this.elementSize = 0;
		this.threshold = size; // size represents the expected number of elements
		int extraRoom = (int) (size * 1.75f);
		if (this.threshold == extraRoom)
			extraRoom++;
		this.values = new HashableWeakReference[extraRoom];	
	}

	/*
	 * Adds the given object to this set. If an object that is equals to the given object already exists, do nothing. Returns
	 * the existing object or the new object if not found.
	 */
	@Override
	public boolean add(T obj)
	{
		cleanupGarbageCollectedValues();
		int valuesLength = this.values.length, index = (obj.hashCode() & 0x7FFFFFFF) % valuesLength;
		HashableWeakReference<T> currentValue;
		while ((currentValue = this.values[index]) != null)
		{
			if (obj.equals(currentValue.get()))
			{
				return false;
			}
			if (++index == valuesLength)
			{
				index = 0;
			}
		}
		this.values[index] = new HashableWeakReference<T>(obj, this.referenceQueue);

		// assumes the threshold is never equal to the size of the table
		if (++this.elementSize > this.threshold)
			rehash();

		return true;
	}

	private void addValue(HashableWeakReference<T> value)
	{
		T obj = value.get();
		if (obj == null)
			return;
		int valuesLength = this.values.length;
		int index = (value.hashCode & 0x7FFFFFFF) % valuesLength;
		HashableWeakReference<T> currentValue;
		while ((currentValue = this.values[index]) != null)
		{
			if (obj.equals(currentValue.get()))
			{
				return;
			}
			if (++index == valuesLength)
			{
				index = 0;
			}
		}
		this.values[index] = value;

		// assumes the threshold is never equal to the size of the table
		if (++this.elementSize > this.threshold)
			rehash();
	}

	@SuppressWarnings("unchecked")
	private void cleanupGarbageCollectedValues()
	{
		HashableWeakReference<T> toBeRemoved;
		while ((toBeRemoved = (HashableWeakReference<T>) this.referenceQueue.poll()) != null)
		{
			int hashCode = toBeRemoved.hashCode;
			int valuesLength = this.values.length;
			int index = (hashCode & 0x7FFFFFFF) % valuesLength;
			HashableWeakReference<T> currentValue;
			while ((currentValue = this.values[index]) != null)
			{
				if (currentValue == toBeRemoved)
				{
					// replace the value at index with the last value with the same hash
					int sameHash = index;
					int current;
					while ((currentValue = this.values[current = (sameHash + 1) % valuesLength]) != null
									&& currentValue.hashCode == hashCode)
						sameHash = current;
					this.values[index] = this.values[sameHash];
					this.values[sameHash] = null;
					this.elementSize--;
					break;
				}
				if (++index == valuesLength)
				{
					index = 0;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object obj)
	{
		return get((T) obj) != null;
	}

	/*
	 * Return the object that is in this set and that is equals to the given object. Return null if not found.
	 */
	public T get(T obj)
	{
		cleanupGarbageCollectedValues();
		int valuesLength = this.values.length;
		int index = (obj.hashCode() & 0x7FFFFFFF) % valuesLength;
		HashableWeakReference<T> currentValue;
		while ((currentValue = this.values[index]) != null)
		{
			T referent;
			if (obj.equals(referent = currentValue.get()))
			{
				return referent;
			}
			if (++index == valuesLength)
			{
				index = 0;
			}
		}
		return null;
	}

	private void rehash()
	{
		// double the number of expected elements
		WeakHashSet<T> newHashSet = new WeakHashSet<T>(this.elementSize * 2);
		newHashSet.referenceQueue = this.referenceQueue;
		HashableWeakReference<T> currentValue;
		for (int i = 0, length = this.values.length; i < length; i++)
			if ((currentValue = this.values[i]) != null)
				newHashSet.addValue(currentValue);

		this.values = newHashSet.values;
		this.threshold = newHashSet.threshold;
		this.elementSize = newHashSet.elementSize;
	}

	/*
	 * Removes the object that is in this set and that is equals to the given object. Returns false if the object was not found.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object obj)
	{
		T toRemove = (T) obj;
		cleanupGarbageCollectedValues();
		int valuesLength = this.values.length;
		int index = (toRemove.hashCode() & 0x7FFFFFFF) % valuesLength;
		HashableWeakReference<T> currentValue;
		while ((currentValue = this.values[index]) != null)
		{
			if (toRemove.equals(currentValue.get()))
			{
				this.elementSize--;
				this.values[index] = null;
				rehash();
				return true;
			}
			if (++index == valuesLength)
			{
				index = 0;
			}
		}
		return false;
	}

	@Override
	public int size()
	{
		return this.elementSize;
	}

	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer("{");
		for (int i = 0, length = this.values.length; i < length; i++)
		{
			HashableWeakReference<T> value = this.values[i];
			if (value != null)
			{
				T ref = value.get();
				if (ref != null)
				{
					buffer.append(ref.toString());
					buffer.append(", ");
				}
			}
		}
		buffer.append("}");
		return buffer.toString();
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean containsAll(Collection<?> arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<T> iterator()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAll(Collection<?> arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] toArray()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}
}