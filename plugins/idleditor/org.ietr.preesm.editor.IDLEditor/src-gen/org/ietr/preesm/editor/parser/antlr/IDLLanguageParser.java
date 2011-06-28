/*
* generated by Xtext
*/
package org.ietr.preesm.editor.parser.antlr;

import com.google.inject.Inject;

import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.ietr.preesm.editor.services.IDLLanguageGrammarAccess;

public class IDLLanguageParser extends org.eclipse.xtext.parser.antlr.AbstractAntlrParser {
	
	@Inject
	private IDLLanguageGrammarAccess grammarAccess;
	
	@Override
	protected void setInitialHiddenTokens(XtextTokenStream tokenStream) {
		tokenStream.setInitialHiddenTokens("RULE_WS", "RULE_ML_COMMENT", "RULE_SL_COMMENT");
	}
	
	@Override
	protected org.ietr.preesm.editor.parser.antlr.internal.InternalIDLLanguageParser createParser(XtextTokenStream stream) {
		return new org.ietr.preesm.editor.parser.antlr.internal.InternalIDLLanguageParser(stream, getGrammarAccess());
	}
	
	@Override 
	protected String getDefaultRuleName() {
		return "IDL";
	}
	
	public IDLLanguageGrammarAccess getGrammarAccess() {
		return this.grammarAccess;
	}
	
	public void setGrammarAccess(IDLLanguageGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}
	
}
