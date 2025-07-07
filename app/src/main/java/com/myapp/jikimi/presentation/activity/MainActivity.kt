package com.myapp.jikimi.presentation.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.myapp.jikimi.R
import com.myapp.jikimi.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // 로그인/회원가입 관련 Fragment ID 목록
    private val hideBottomNavFragments = listOf(
        R.id.loginFragment,
        R.id.registerFragment,
        R.id.detailFragment,
        R.id.createPostFragment,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        // splashScreen 실행
        installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation(savedInstanceState)
    }


    private fun setupNavigation(savedInstanceState: Bundle?) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment
        navController = navHostFragment.navController

        // 시작 프래그먼트 설정 - 로그인 상태에 따라 다르게 설정
        if (savedInstanceState == null) {
            val startDestination = if (isUserLoggedIn()) {
                R.id.evacuateFragment
            } else {
                R.id.loginFragment
            }

            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(startDestination)
            navController.graph = navGraph
        }

        // BottomNavigation 설정
        binding.bottomNavBar.setupWithNavController(navController)

        // bottomNavBar 선택했을 때 해당뷰로 이동
        binding.bottomNavBar.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(item, navController)
            true
        }

        // Fragment 이동 시 BottomNavigationView 표시 여부 처리
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in hideBottomNavFragments) {
                hideBottomNavigation()
            }
        }
    }

    private fun isUserLoggedIn(): Boolean {
        // Firebase Auth를 통해 로그인 상태 확인
        return FirebaseAuth.getInstance().currentUser != null
    }

    fun hideBottomNavigation() {
        binding.bottomNavBar.visibility = View.GONE
    }

    fun showBottomNavigation() {
        val currentDestination = navController.currentDestination?.id

        if (currentDestination !in hideBottomNavFragments) {
            binding.bottomNavBar.visibility = View.VISIBLE
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}