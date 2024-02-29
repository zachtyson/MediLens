package com.ztch.medilens_android_app.Homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import com.ztch.medilens_android_app.R
import com.ztch.medilens_android_app.appbarBottom
import java.time.format.DateTimeFormatter


@Composable
fun HomePage(onNavigateToCamera: () -> Unit) {
    val dataSource = CalendarDataSource()
    // we use `mutableStateOf` and `remember` inside composable function to schedules recomposition
    var calendarUiModel by remember { mutableStateOf(dataSource.getData(lastSelectedDate = dataSource.today)) }

    Column(

        modifier = Modifier.fillMaxSize()
            .background(color = colorResource(R.color.DarkGrey))
    ) {

        homepageHeader(data = calendarUiModel, onDateClickListener = { date ->
            // refresh the CalendarUiModel with new data
            // by changing only the `selectedDate` with the date selected by User
            calendarUiModel = calendarUiModel.copy(
                selectedDate = date,
                visibleDates = calendarUiModel.visibleDates.map {
                    it.copy(
                        isSelected = it.date.isEqual(date.date)
                    )
                }
            )
        }) // end of header

        //Spacer(modifier = Modifier.height(425.dp))
        homeAppBar { onNavigateToCamera() }
    }
}


@Preview(showSystemUi = true, device = "id:pixel_7_pro")
@Composable
fun homepagePreview() {
    HomePage(onNavigateToCamera = {})
}


// Start of Header creation
@Composable
fun homepageHeader(data: CalendarUiModel,onDateClickListener: (CalendarUiModel.Date) -> Unit) {

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(color = colorResource(R.color.DarkBlue)),

    ) {

        Row{
        Text(
            // show "Today" if user selects today's date
            text = if (data.selectedDate.isToday) {
                "Today"
            }else{
                data.selectedDate.dayHeader
            },
            fontSize = 32.sp,
            color = Color.White,
            modifier = Modifier.padding(start = 14.dp)
                .weight(1f)

        )
        }
        Row {
            Text(
                // else, show the full format of the date
                text = data.selectedDate.date.format(DateTimeFormatter.ofPattern("MMMM"))+"   " +
                        data.selectedDate.date.format(DateTimeFormatter.ofPattern("d")),//data.selectedDate.date.format(

                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier.padding(start = 14.dp)
                    .weight(1f)
                    .align(Alignment.CenterVertically)

            )
        }


        Spacer(modifier = Modifier.height(8.dp))

       // Spacer(modifier = Modifier.height(14.dp))
        RowOfDates(data = data,onDateClickListener = onDateClickListener)
    }
}


@Composable
fun DateCard(data: CalendarUiModel.Date,onDateClickListener: (CalendarUiModel.Date) -> Unit) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 4.dp),
        onClick = { onDateClickListener(data) },
        colors = CardDefaults.cardColors(
            // background colors of the selected date
            // and the non-selected date are different
            containerColor = if (data.isSelected) {
                colorResource(R.color.DarkestBlue)
            } else {
                colorResource(R.color.Blue)

            }
        )
  ) {
        Column(
            modifier = Modifier
                .width(40.dp)
                .height(48.dp)
                .padding(4.dp)
        ) {
            Text(
                text = data.day,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = data.date.dayOfMonth.toString(),// date "15", "16"
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
@Composable
fun RowOfDates(data: CalendarUiModel,onDateClickListener: (CalendarUiModel.Date) -> Unit) {
  LazyRow {
      items(items = data.visibleDates ) { date ->
          DateCard(date,onDateClickListener)


      }
  }
}




//Start of bottom header
@Composable
fun homeAppBar( onNavigateToCamera: () -> Unit){
    val colorPurple = colorResource(R.color.Purple)
    Scaffold(
        containerColor = colorResource(R.color.DarkGrey),
        modifier = Modifier.fillMaxSize(),


        bottomBar = { appbarBottom{ onNavigateToCamera() } },
    ){innerPadding ->
        Column(
            modifier = Modifier
                .background(color = colorResource(R.color.DarkGrey))
                .padding(innerPadding),


            verticalArrangement = Arrangement.spacedBy(16.dp),

        ){
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.DarkestBlue)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)

            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Medication",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You have no medication today",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

}







