package com.example.easymoney.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.easymoney.R
import com.example.easymoney.ui.common.loading.SkeletonBlock

@Composable
fun HomeLoadingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SkeletonBlock(height = 180.dp, cornerRadius = 16.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonBlock(
                modifier = Modifier.weight(1f),
                height = 100.dp,
                cornerRadius = 16.dp
            )
            SkeletonBlock(
                modifier = Modifier.weight(1f),
                height = 100.dp,
                cornerRadius = 16.dp
            )
        }

        SkeletonBlock(height = 120.dp, cornerRadius = 16.dp)
    }
}

@Composable
fun MainBanner(onRegistrationClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onRegistrationClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.vaytochuctindung),
            contentDescription = stringResource(id = R.string.home_main_banner_title),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
fun GridSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BannerItem(
            frameRes = R.drawable.quanlykhoanvay_frame,
            iconRes = R.drawable.quanlykhoanvay,
            modifier = Modifier.weight(1f)
        )
        BannerItem(
            frameRes = R.drawable.goiykhoanvay_frame,
            iconRes = R.drawable.goiykhoanvay,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WideBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.tuvankhoanvay_frame),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Image(
                painter = painterResource(id = R.drawable.tuvankhoanvay),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(110.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = (-8).dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun BannerItem(frameRes: Int, iconRes: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = frameRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = (-8).dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
