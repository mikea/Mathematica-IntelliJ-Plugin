/*
 * Copyright (c) 2013 Patrick Scheibe
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.halirutan.mathematica.parsing.psi.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.psi.PsiElement;
import de.halirutan.mathematica.parsing.psi.MathematicaVisitor;
import de.halirutan.mathematica.parsing.psi.api.FunctionCall;
import de.halirutan.mathematica.parsing.psi.api.Group;
import de.halirutan.mathematica.parsing.psi.api.Symbol;
import de.halirutan.mathematica.parsing.psi.api.assignment.SetDelayed;
import de.halirutan.mathematica.parsing.psi.api.assignment.TagSet;
import de.halirutan.mathematica.parsing.psi.api.assignment.TagSetDelayed;
import de.halirutan.mathematica.parsing.psi.api.pattern.*;

import java.util.List;
import java.util.Set;

/**
 * @author patrick (10/10/13)
 */
public class MathematicaPatternVisitor extends MathematicaVisitor {

  private final Set<Symbol> myPatternSymbols = Sets.newHashSet();
  private final Set<Symbol> myDefinitionSymbols = Sets.newHashSet();
  private final List<String> myDiveInFirstChild = Lists.newArrayList("Longest", "Shortest", "Repeated", "Optional", "PatternTest", "Condition");
  private final List<String> myDoNotDiveIn = Lists.newArrayList("Verbatim");

  public Set<Symbol> getMyPatternSymbols() {
    return myPatternSymbols;
  }

  @Override
  public void visitBlank(Blank blank) {
    if (blank.getFirstChild() instanceof Symbol) {
      myPatternSymbols.add((Symbol) blank.getFirstChild());
    }
  }

  @Override
  public void visitBlankSequence(BlankSequence blankSequence) {
    if (blankSequence.getFirstChild() instanceof Symbol) {
      myPatternSymbols.add((Symbol) blankSequence.getFirstChild());
    }
  }

  @Override
  public void visitBlankNullSequence(BlankNullSequence blankNullSequence) {
    if (blankNullSequence.getFirstChild() instanceof Symbol) {
      myPatternSymbols.add((Symbol) blankNullSequence.getFirstChild());
    }
  }

  @Override
  public void visitOptional(Optional optional) {
    PsiElement firstChild = optional.getFirstChild();
    if (firstChild != null) {
      firstChild.accept(this);
    }
  }

  @Override
  public void visitCondition(Condition condition) {
    PsiElement firstChild = condition.getFirstChild();
    if (firstChild != null) {
      firstChild.accept(this);
    }
  }

  @Override
  public void visitPattern(Pattern pattern) {
    PsiElement firstChild = pattern.getFirstChild();
    if (firstChild instanceof Symbol) {
      myPatternSymbols.add((Symbol) firstChild);
    }
    pattern.getLastChild().accept(this);
  }

  @Override
  public void visitFunctionCall(FunctionCall functionCall) {
    final PsiElement head = functionCall.getFirstChild();
    if (head instanceof Symbol) {
      final String functionName = ((Symbol) head).getSymbolName();
      if (myDiveInFirstChild.contains(functionName)) {
        List<PsiElement> args = MathematicaPsiUtililities.getArguments(functionCall);
        if (args.size() > 0) {
          args.get(0).accept(this);
        }
      } else if (!myDoNotDiveIn.contains(functionName)) {
        functionCall.acceptChildren(this);
      }
    } else {
      functionCall.acceptChildren(this);
    }
  }

  @Override
  public void visitGroup(Group group) {
    group.acceptChildren(this);
  }

  @Override
  public void visitList(de.halirutan.mathematica.parsing.psi.api.lists.List list) {
    list.acceptChildren(this);
  }

  @Override
  public void visitSetDelayed(SetDelayed setDelayed) {
    final PsiElement lhs = setDelayed.getFirstChild();
    lhs.accept(this);
  }

  @Override
  public void visitSet(de.halirutan.mathematica.parsing.psi.api.assignment.Set set) {
    final PsiElement lhs = set.getFirstChild();
    lhs.accept(this);
  }

  @Override
  public void visitTagSet(TagSet element) {
    final PsiElement firstChild = element.getFirstChild();
    if (firstChild == null) {
      return;
    }
    final PsiElement operator = MathematicaPsiUtililities.getNextSiblingSkippingWhitespace(firstChild);
    if (operator == null) {
      return;
    }
    final PsiElement pattern = MathematicaPsiUtililities.getNextSiblingSkippingWhitespace(operator);
    if (pattern != null) {
      pattern.accept(this);
    }
  }

  @Override
  public void visitTagSetDelayed(TagSetDelayed tagSetDelayed) {
    final PsiElement firstChild = tagSetDelayed.getFirstChild();
    if (firstChild == null) {
      return;
    }
    final PsiElement operator = MathematicaPsiUtililities.getNextSiblingSkippingWhitespace(firstChild);
    if (operator == null) {
      return;
    }
    final PsiElement pattern = MathematicaPsiUtililities.getNextSiblingSkippingWhitespace(operator);
    if (pattern != null) {
      pattern.accept(this);
    }
  }

  @Override
  public void visitElement(PsiElement element) {
    element.acceptChildren(this);
  }
}

