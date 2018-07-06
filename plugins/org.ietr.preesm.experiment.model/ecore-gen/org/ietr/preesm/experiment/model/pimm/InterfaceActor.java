/**
 */
package org.ietr.preesm.experiment.model.pimm;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Interface Actor</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.InterfaceActor#getGraphPort <em>Graph Port</em>}</li>
 * </ul>
 *
 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getInterfaceActor()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface InterfaceActor extends AbstractActor {
  /**
   * Returns the value of the '<em><b>Graph Port</b></em>' reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Graph Port</em>' reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Graph Port</em>' reference.
   * @see #setGraphPort(Port)
   * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getInterfaceActor_GraphPort()
   * @model
   * @generated
   */
  Port getGraphPort();

  /**
   * Sets the value of the '{@link org.ietr.preesm.experiment.model.pimm.InterfaceActor#getGraphPort <em>Graph Port</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Graph Port</em>' reference.
   * @see #getGraphPort()
   * @generated
   */
  void setGraphPort(Port value);

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @model kind="operation" unique="false"
   *        annotation="http://www.eclipse.org/emf/2002/GenModel body='return this.getAllDataPorts().get(0);'"
   * @generated
   */
  DataPort getDataPort();

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @model kind="operation" unique="false"
   * @generated
   */
  InterfaceKind getKind();

} // InterfaceActor
