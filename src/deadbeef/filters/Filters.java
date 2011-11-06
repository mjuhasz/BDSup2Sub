/*
 * Copyright 2009, Morten Nobel-Joergensen / Volker Oth (0xdeadbeef)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package deadbeef.filters;

/**
 * Utility class for handling filters.
 *
 * @author Morten Nobel-Joergensen / 0xdeadbeef
 */
public class Filters {
	private final static BellFilter     bellFilter     = new BellFilter();
	private final static BiCubicFilter  biCubicFilter  = new BiCubicFilter();
	private final static BSplineFilter  bSplineFilter  = new BSplineFilter();
	private final static HermiteFilter  hermiteFilter  = new HermiteFilter();
	private final static Lanczos3Filter lanczos3Filter = new Lanczos3Filter();
	private final static MitchellFilter mitchellFilter = new MitchellFilter();
	private final static TriangleFilter triangleFilter = new TriangleFilter();

	/**
	 * Get Bell filter
	 * @return Bell filter
	 */
	public static Filter getBellFilter(){
		return bellFilter;
	}

	/**
	 * Get Bicubic filter
	 * @return Bicubic filter
	 */
	public static Filter getBiCubicFilter(){
		return biCubicFilter;
	}

	/**
	 * Get Bicubic spline filter
	 * @return Bicubic spline filter
	 */
	public static Filter getBSplineFilter(){
		return bSplineFilter;
	}

	/**
	 * Get Hermite filter
	 * @return Hermite filter
	 */
	public static Filter getHermiteFilter(){
		return hermiteFilter;
	}

	/**
	 * Get Lanczos3 filter
	 * @return Lanczos3 filter
	 */
	public static Filter getLanczos3Filter(){
		return lanczos3Filter;
	}

	/**
	 * Get Mitchell filter
	 * @return Mitchell filter
	 */
	public static Filter getMitchellFilter(){
		return mitchellFilter;
	}

	/**
	 * Get triangle filter
	 * @return Hermite filter
	 */
	public static Filter getTriangleFilter(){
		return triangleFilter;
	}
}
