// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.koxudaxi.poetry

import com.jetbrains.python.sdk.flavors.CPythonSdkFlavor
import java.io.File

/**
 * @author vlan
 */

/**
 *  This source code is edited by @koxudaxi  (Koudai Aono)
 */

object PyPoetrySdkFlavor : CPythonSdkFlavor() {
  // TODO: Need a extension point
   override fun getIcon() = POETRY_ICON

  override fun isValidSdkPath(file: File) = false
}