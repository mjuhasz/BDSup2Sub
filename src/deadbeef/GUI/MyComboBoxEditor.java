package deadbeef.GUI;

import java.awt.Component;
import java.awt.Insets;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;

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
 * ComboBox Editor - part of BDSup2Sub GUI classes.
 * Implementation needed to be able to change the background color of editable ComboBoxes.
 *
 * @author 0xdeadbeef
 */
class MyComboBoxEditor extends BasicComboBoxEditor {

	/**
	 * Constructor
	 */
	public MyComboBoxEditor() {
		super();
	}

	/**
	 * Constructor with JTextField
	 * @param editor JTextField to use as editor
	 */
	public MyComboBoxEditor(final JTextField editor) {
		super();
		this.editor = editor;
		editor.setBorder(new EmptyBorder(new Insets(1,2,1,2)));
	}

	/* (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicComboBoxEditor#getEditorComponent()
	 */
	@Override
	public Component getEditorComponent() {
		return editor;
	}

	/**
	 * Get JTextField used as editor
	 * @return JTextField used as editor
	 */
	public JTextField getJTextField() {
		return editor;
	}

	/**
	 * Set JTextField used as editor
	 * @param editor JTextField used as editor
	 */
	public void setEditorComponent(final JTextField editor) {
		this.editor = editor;
	}

}
