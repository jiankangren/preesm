/*******************************************************************************
 * Copyright or © or Copr. 2013 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Julien Heulot <julien.heulot@insa-rennes.fr> (2013 - 2015)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2014)
 * Romina Racca <romina.racca@gmail.com> (2013)
 *
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package org.ietr.preesm.experiment.model.pimm.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.ietr.preesm.experiment.model.expression.ExpressionEvaluator;
import org.ietr.preesm.experiment.model.pimm.Expression;
import org.ietr.preesm.experiment.model.pimm.PiMMPackage;
import org.ietr.preesm.experiment.model.pimm.visitor.PiMMVisitor;
import org.nfunk.jep.ParseException;

// TODO: Auto-generated Javadoc
/**
 * <!-- begin-user-doc --> An implementation of the model object ' <em><b>Expression</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link org.ietr.preesm.experiment.model.pimm.impl.ExpressionImpl#getExpressionString <em>Expression String</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ExpressionImpl extends EObjectImpl implements Expression {

  /**
   * The default value of the '{@link #getExpressionString() <em>Expression String</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @see #getExpressionString()
   * @generated
   * @ordered
   */
  protected static final String EXPRESSION_STRING_EDEFAULT = "0";
  /**
   * The cached value of the '{@link #getExpressionString() <em>Expression String</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @see #getExpressionString()
   * @generated
   * @ordered
   */
  protected String              expressionString           = ExpressionImpl.EXPRESSION_STRING_EDEFAULT;

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->.
   *
   * @generated
   */
  protected ExpressionImpl() {
    super();
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->.
   *
   * @return the e class
   * @generated
   */
  @Override
  protected EClass eStaticClass() {
    return PiMMPackage.Literals.EXPRESSION;
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->.
   *
   * @return the string
   * @generated
   */
  @Override
  public String getExpressionString() {
    return this.expressionString;
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->.
   *
   * @param newString
   *          the new string
   * @generated
   */
  @Override
  public void setExpressionString(final String newExpressionString) {
    final String oldExpressionString = this.expressionString;
    this.expressionString = newExpressionString;
    if (eNotificationRequired()) {
      eNotify(new ENotificationImpl(this, Notification.SET, PiMMPackage.EXPRESSION__EXPRESSION_STRING, oldExpressionString, this.expressionString));
    }
  }

  /**
   * <!-- begin-user-doc --> Evaluate expression of the class for which it is called. <!-- end-user-doc -->
   *
   * @return the result of the expression evaluated as an int.
   * @throws ParseException
   */
  @Override
  public String evaluate() {
    long evaluate;
    evaluate = ExpressionEvaluator.evaluate(this);
    return Long.toString(evaluate);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->.
   *
   * @param featureID
   *          the feature ID
   * @param resolve
   *          the resolve
   * @param coreType
   *          the core type
   * @return the object
   * @generated
   */
  @Override
  public Object eGet(final int featureID, final boolean resolve, final boolean coreType) {
    switch (featureID) {
      case PiMMPackage.EXPRESSION__EXPRESSION_STRING:
        return getExpressionString();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->.
   *
   * @param featureID
   *          the feature ID
   * @param newValue
   *          the new value
   * @generated
   */
  @Override
  public void eSet(final int featureID, final Object newValue) {
    switch (featureID) {
      case PiMMPackage.EXPRESSION__EXPRESSION_STRING:
        setExpressionString((String) newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->.
   *
   * @param featureID
   *          the feature ID
   * @generated
   */
  @Override
  public void eUnset(final int featureID) {
    switch (featureID) {
      case PiMMPackage.EXPRESSION__EXPRESSION_STRING:
        setExpressionString(ExpressionImpl.EXPRESSION_STRING_EDEFAULT);
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->.
   *
   * @param featureID
   *          the feature ID
   * @return true, if successful
   * @generated
   */
  @Override
  public boolean eIsSet(final int featureID) {
    switch (featureID) {
      case PiMMPackage.EXPRESSION__EXPRESSION_STRING:
        return ExpressionImpl.EXPRESSION_STRING_EDEFAULT == null ? this.expressionString != null
            : !ExpressionImpl.EXPRESSION_STRING_EDEFAULT.equals(this.expressionString);
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->.
   *
   * @return the string
   * @generated
   */
  @Override
  public String toString() {
    if (eIsProxy()) {
      return super.toString();
    }

    final StringBuffer result = new StringBuffer(super.toString());
    result.append(" (expressionString: ");
    result.append(this.expressionString);
    result.append(')');
    return result.toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.preesm.experiment.model.pimm.util.PiMMVisitable#accept(org.ietr.preesm.experiment.model.pimm.util.PiMMVisitor)
   */
  @Override
  public void accept(final PiMMVisitor v) {
    v.visitExpression(this);
  }

} // ExpressionImpl
