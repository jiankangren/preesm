/**
 */
package org.ietr.preesm.experiment.model.pimm.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.ietr.preesm.experiment.model.pimm.ConfigOutputInterface;
import org.ietr.preesm.experiment.model.pimm.DataPort;
import org.ietr.preesm.experiment.model.pimm.InterfaceKind;
import org.ietr.preesm.experiment.model.pimm.PiMMPackage;
import org.ietr.preesm.experiment.model.pimm.Port;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Config Output Interface</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link org.ietr.preesm.experiment.model.pimm.impl.ConfigOutputInterfaceImpl#getGraphPort <em>Graph Port</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ConfigOutputInterfaceImpl extends AbstractActorImpl implements ConfigOutputInterface {
  /**
   * The cached value of the '{@link #getGraphPort() <em>Graph Port</em>}' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @see #getGraphPort()
   * @generated
   * @ordered
   */
  protected Port graphPort;

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  protected ConfigOutputInterfaceImpl() {
    super();
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  protected EClass eStaticClass() {
    return PiMMPackage.Literals.CONFIG_OUTPUT_INTERFACE;
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public Port getGraphPort() {
    if ((this.graphPort != null) && this.graphPort.eIsProxy()) {
      final InternalEObject oldGraphPort = (InternalEObject) this.graphPort;
      this.graphPort = (Port) eResolveProxy(oldGraphPort);
      if (this.graphPort != oldGraphPort) {
        if (eNotificationRequired()) {
          eNotify(new ENotificationImpl(this, Notification.RESOLVE, PiMMPackage.CONFIG_OUTPUT_INTERFACE__GRAPH_PORT, oldGraphPort, this.graphPort));
        }
      }
    }
    return this.graphPort;
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  public Port basicGetGraphPort() {
    return this.graphPort;
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public void setGraphPort(final Port newGraphPort) {
    final Port oldGraphPort = this.graphPort;
    this.graphPort = newGraphPort;
    if (eNotificationRequired()) {
      eNotify(new ENotificationImpl(this, Notification.SET, PiMMPackage.CONFIG_OUTPUT_INTERFACE__GRAPH_PORT, oldGraphPort, this.graphPort));
    }
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public InterfaceKind getKind() {
    return InterfaceKind.CFG_OUTPUT;
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public DataPort getDataPort() {
    // Data in/out interfaces have only one data port
    return getAllDataPorts().get(0);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public Object eGet(final int featureID, final boolean resolve, final boolean coreType) {
    switch (featureID) {
      case PiMMPackage.CONFIG_OUTPUT_INTERFACE__GRAPH_PORT:
        if (resolve) {
          return getGraphPort();
        }
        return basicGetGraphPort();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public void eSet(final int featureID, final Object newValue) {
    switch (featureID) {
      case PiMMPackage.CONFIG_OUTPUT_INTERFACE__GRAPH_PORT:
        setGraphPort((Port) newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public void eUnset(final int featureID) {
    switch (featureID) {
      case PiMMPackage.CONFIG_OUTPUT_INTERFACE__GRAPH_PORT:
        setGraphPort((Port) null);
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public boolean eIsSet(final int featureID) {
    switch (featureID) {
      case PiMMPackage.CONFIG_OUTPUT_INTERFACE__GRAPH_PORT:
        return this.graphPort != null;
    }
    return super.eIsSet(featureID);
  }

} // ConfigOutputInterfaceImpl
