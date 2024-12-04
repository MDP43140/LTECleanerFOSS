/*
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package theredspy15.ltecleanerfoss.fragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
// this is not a specific piece of fragment,
// but more like the custom Fragment template
abstract class BaseFragment(): Fragment(){
	override fun onCreate(savedInstanceState: Bundle?){
		super.onCreate(savedInstanceState)
		enterTransition   = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true )
		returnTransition  = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
		exitTransition    = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true )
		reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
	}
}