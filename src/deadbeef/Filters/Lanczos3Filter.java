package deadbeef.Filters;

/*
 * Copyright 2009 Volker Oth (0xdeadbeef)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Lanczos3 resample filter.
 *
 * @author 0xdeadbeef
 */
final class Lanczos3Filter implements Filter {
	private final static float PI_FLOAT = (float) Math.PI;

	private float sincModified(float value) {
		return ((float)Math.sin(value)) / value;
	}

	public final float value(float value) {
		if (value==0)
			return 1.0f;

		if (value < 0.0f)
			value = -value;

		if (value < 3.0f) {
			value *= PI_FLOAT;
			return sincModified(value) * sincModified(value / 3.0f);
		}
		return 0.0f;
	}

	public float getRadius() {
		return 3.0f;
	}
}
