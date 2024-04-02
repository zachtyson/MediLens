import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient
from httpx import AsyncClient
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import Session

from core.security import get_password_hash
from models.user import User
from tests.test_main import async_client, test_db

from backend.api.pill_from_imprint.demo import extract_info
from bs4 import BeautifulSoup
import requests


@pytest.mark.asyncio
async def test_get_pill_from_imprint(async_client: AsyncClient, test_db: AsyncSession):
    # 9 3 - 5510 is a pill with imprint "9 3 - 5510" and is
    # Mercaptopurine 50 MG Oral Tablet
    imprint = "9 3 5510"
    color = 0
    shape = 0

    response = await async_client.get(f"/pill_from_imprint-demo?imprint={imprint}&color={color}&shape={shape}")
    assert response.status_code == 200

    # Iterate over entire returned array and check if arr[i]['pillName'] contains Mercaptopurine

    assert any([True for i in response.json() if 'Mercaptopurine' in i['pillName']])


@pytest.mark.asyncio
async def test_get_pill_from_imprint_2(async_client: AsyncClient, test_db: AsyncSession):
    # 'H 49' with White Oval is Sulfamethoxazole and Trimethoprim
    imprint = "H 49"
    color = 12  # White
    shape = 11  # Oval

    response = await async_client.get(f"/pill_from_imprint-demo?imprint={imprint}&color={color}&shape={shape}")
    assert response.status_code == 200

    # Iterate over entire returned array and check if arr[i]['pillName'] contains Sulfamethoxazole and Trimethoprim
    assert any([True for i in response.json() if 'Sulfamethoxazole and Trimethoprim' in i['pillName']])


@pytest.mark.asyncio
async def test_extract_info_1():
    url = f"https://www.drugs.com/imprints.php?imprint=H+49&color=12&shape=11"
    response = requests.get(url)
    res = response.text
    soup = BeautifulSoup(res, 'html.parser')
    div_content = soup.find_all('div', class_='ddc-card')
    extracted_info = extract_info(div_content)

    # Check to see if 'Sulfamethoxazole and Trimethoprim' is in the pillName for any of the pills
    assert any([True for i in extracted_info if 'Sulfamethoxazole and Trimethoprim' in i['pillName']])


@pytest.mark.asyncio
async def test_extract_info_2():
    url = f"https://www.drugs.com/imprints.php?imprint=9+3+5510&color=0&shape=0"
    response = requests.get(url)
    res = response.text
    soup = BeautifulSoup(res, 'html.parser')
    div_content = soup.find_all('div', class_='ddc-card')
    extracted_info = extract_info(div_content)

    # Check to see if 'Mercaptopurine' is in the pillName for any of the pills
    assert any([True for i in extracted_info if 'Mercaptopurine' in i['pillName']])


@pytest.mark.asyncio
async def test_extract_info_3():
    raw_html = """
<div class='ddc-pid-list'>
<div class='ddc-card' data-pid-imprintid='22292'>
<div class='ddc-pid-img pid-img-fit' data-pid-image data-image-src='https://www.drugs.com/images/pills/fio/TOR01560/telmisartan.JPG' data-image-scale='1.6'>
		<img src='https://www.drugs.com/images/pills/fio/TOR01560/telmisartan.JPG'
			  data-orig-width='288'
			  data-orig-height='216'
			  width='373'
			  height='270'
			  			  alt='Pill 156 20 White Round is Telmisartan'>

			</div>
	<div class='ddc-pid-card-header'>
					<h2>156 20</h2>
		
					<div class='ddc-pid-card-nav'>
							</div>
			</div>
	<div class='ddc-card-content ddc-card-content-pid'>
					<a href='/mtm/telmisartan.html' class='ddc-text-size-small'>Telmisartan</a>
				<dl class='ddc-text-size-small'>
							<dt>Strength</dt><dd>20 mg</dd>
				<dt>Imprint</dt><dd><a href='/imprints/156-20-22292.html'>156 20</a></dd>
										<dt>Color</dt><dd>White</dd>
										<dt>Shape</dt><dd>Round</dd>
								</dl>

		<a class='ddc-btn ddc-btn-small' href='/imprints/156-20-22292.html' data-btn-click='Processing...'>View details</a>
	</div>
</div>
<div class='ddc-card' data-pid-imprintid='1186'>
	<div class='ddc-pid-img pid-img-fit' data-pid-image data-image-src='https://www.drugs.com/images/pills/mmx/t101670f/lipitor.jpg' data-image-scale='1.5'>
		<img src='https://www.drugs.com/images/pills/mmx/t101670f/lipitor.jpg'
			  data-orig-width='345'
			  data-orig-height='240'
			  width='373'
			  height='270'
			  			  alt='Pill PD 156 20 White Oval is Lipitor'>

					<div class='ddc-pid-img-counter'>
				<span data-pid-index>1</span> / 4
			</div>

			<div class='ddc-loading-wrap ddc-flex ddc-flex-items-center ddc-flex-justify-center'>
				<div class='ddc-loading' role='status' aria-label='Loading image'>
					<span class='ddc-sr-only'>Loading</span>
				</div>
			</div>
			</div>
	<div class='ddc-pid-card-header'>
					<h2>PD 156 20</h2>
		
					<div class='ddc-pid-card-nav'>
									<button data-pid-direction='prev' class='is-disabled'>
						<span class='ddc-sr-only'>Previous</span>
						<svg class='ddc-icon ddc-icon-prev' width='20' height='20' viewBox='0 0 24 24' aria-hidden='true' focusable='false' xmlns='http://www.w3.org/2000/svg'><path d='M7.5 12 15 4.5l1.05 1.05L9.6 12l6.45 6.45L15 19.5 7.5 12Z' /></svg>
					</button>
					<button data-pid-direction='next'>
						<span class='ddc-sr-only'>Next</span>
						<svg class='ddc-icon ddc-icon-next' width='20' height='20' viewBox='0 0 24 24' aria-hidden='true' focusable='false' xmlns='http://www.w3.org/2000/svg'><path d='M16.5 12 9 19.5l-1.05-1.05L14.4 12 7.95 5.55 9 4.5l7.5 7.5Z' /></svg>
					</button>
							</div>
			</div>
	<div class='ddc-card-content ddc-card-content-pid'>
					<a href='/lipitor.html' class='ddc-text-size-small'>Lipitor</a>
				<dl class='ddc-text-size-small'>
							<dt>Strength</dt><dd>20 mg</dd>
				<dt>Imprint</dt><dd><a href='/imprints/pd-156-20-1186.html'>PD 156 20</a></dd>
										<dt>Color</dt><dd>White</dd>
										<dt>Shape</dt><dd>Oval</dd>
								</dl>

		<a class='ddc-btn ddc-btn-small' href='/imprints/pd-156-20-1186.html' data-btn-click='Processing...'>View details</a>
	</div>
</div>
<div class='ddc-card' data-pid-imprintid='18837'>
	<div class='ddc-pid-img pid-img-fit' data-pid-image data-image-src='https://www.drugs.com/images/pills/nlm/433530892.jpg' data-image-scale='1.1'>
		<img src='https://www.drugs.com/images/pills/nlm/433530892.jpg'
			  data-orig-width='577'
			  data-orig-height='433'
			  width='373'
			  height='270'
			  			  alt='Pill PD 156 20 White Oval is Atorvastatin Calcium'>

					<div class='ddc-pid-img-counter'>
				<span data-pid-index>1</span> / 6
			</div>

			<div class='ddc-loading-wrap ddc-flex ddc-flex-items-center ddc-flex-justify-center'>
				<div class='ddc-loading' role='status' aria-label='Loading image'>
					<span class='ddc-sr-only'>Loading</span>
				</div>
			</div>
			</div>
	<div class='ddc-pid-card-header'>
					<h2>PD 156 20</h2>
		
					<div class='ddc-pid-card-nav'>
									<button data-pid-direction='prev' class='is-disabled'>
						<span class='ddc-sr-only'>Previous</span>
						<svg class='ddc-icon ddc-icon-prev' width='20' height='20' viewBox='0 0 24 24' aria-hidden='true' focusable='false' xmlns='http://www.w3.org/2000/svg'><path d='M7.5 12 15 4.5l1.05 1.05L9.6 12l6.45 6.45L15 19.5 7.5 12Z' /></svg>
					</button>
					<button data-pid-direction='next'>
						<span class='ddc-sr-only'>Next</span>
						<svg class='ddc-icon ddc-icon-next' width='20' height='20' viewBox='0 0 24 24' aria-hidden='true' focusable='false' xmlns='http://www.w3.org/2000/svg'><path d='M16.5 12 9 19.5l-1.05-1.05L14.4 12 7.95 5.55 9 4.5l7.5 7.5Z' /></svg>
					</button>
							</div>
			</div>
	<div class='ddc-card-content ddc-card-content-pid'>
					<a href='/atorvastatin.html' class='ddc-text-size-small'>Atorvastatin Calcium</a>
				<dl class='ddc-text-size-small'>
							<dt>Strength</dt><dd>20 mg</dd>
				<dt>Imprint</dt><dd><a href='/imprints/pd-156-20-18837.html'>PD 156 20</a></dd>
										<dt>Color</dt><dd>White</dd>
										<dt>Shape</dt><dd>Oval</dd>
								</dl>

		<a class='ddc-btn ddc-btn-small' href='/imprints/pd-156-20-18837.html' data-btn-click='Processing...'>View details</a>
	</div>
</div>
<div class='ddc-pid-list-ad'></div>
<div class='ddc-card' data-pid-imprintid='24688'>
	<div class='ddc-pid-img ddc-pid-img-none' data-pid-image data-image-src='/img/pillid/no-image-placeholder.png' data-image-scale='1.7'>
		<img src='/img/pillid/no-image-placeholder.png'
			  data-orig-width='260'
			  data-orig-height='160'
			  width='373'
			  height='270'
			  loading='lazy'			  alt='Pill A156 20 Beige Capsule/Oblong is Duloxetine Hydrochloride Delayed-Release'>

			</div>
	<div class='ddc-pid-card-header'>
					<h2>A156 20</h2>
		
			</div>
	<div class='ddc-card-content ddc-card-content-pid'>
					<a href='/duloxetine.html' class='ddc-text-size-small'>Duloxetine Hydrochloride Delayed-Release</a>
				<dl class='ddc-text-size-small'>
							<dt>Strength</dt><dd>20 mg</dd>
				<dt>Imprint</dt><dd><a href='/imprints/a156-20-24688.html'>A156 20</a></dd>
										<dt>Color</dt><dd>Beige / Orange</dd>
										<dt>Shape</dt><dd>Capsule/Oblong</dd>
								</dl>

		<a class='ddc-btn ddc-btn-small' href='/imprints/a156-20-24688.html' data-btn-click='Processing...'>View details</a>
	</div>
</div>
<div class='ddc-card' data-pid-imprintid='35555'>
	<div class='ddc-pid-img ddc-pid-img-none' data-pid-image data-image-src='/img/pillid/no-image-placeholder.png' data-image-scale='1.7'>
		<img src='/img/pillid/no-image-placeholder.png'
			  data-orig-width='260'
			  data-orig-height='160'
			  width='373'
			  height='270'
			  loading='lazy'			  alt='Pill VLE 156 20 White Oval is Lipitor'>

			</div>
	<div class='ddc-pid-card-header'>
					<h2>VLE 156 20</h2>
		
			</div>
	<div class='ddc-card-content ddc-card-content-pid'>
					<a href='/lipitor.html' class='ddc-text-size-small'>Lipitor</a>
				<dl class='ddc-text-size-small'>
							<dt>Strength</dt><dd>20 mg</dd>
				<dt>Imprint</dt><dd><a href='/imprints/vle-156-20-35555.html'>VLE 156 20</a></dd>
										<dt>Color</dt><dd>White</dd>
										<dt>Shape</dt><dd>Oval</dd>
								</dl>

		<a class='ddc-btn ddc-btn-small' href='/imprints/vle-156-20-35555.html' data-btn-click='Processing...'>View details</a>
	</div>
</div>
</div>
"""
    soup = BeautifulSoup(raw_html, 'html.parser')
    extracted_info = extract_info(soup.find_all('div', class_='ddc-card'))

    # Assert that the extracted_info is not empty and is a list of size 5
    assert extracted_info
    assert len(extracted_info) == 5

    # Assert there is a pill with the following info:
    # Name: Telmisartan
    # Strength: 20 mg
    # Imprint: 156 20
    # Color: White
    # Shape: Round

    assert any([True for i in extracted_info if i['pillName'] == 'Telmisartan' and
                i['strength'] == '20 mg' and i['imprint'] == '156 20' and i['color'] == 'White' and i['shape'] == 'Round'])

    # Assert there is a pill with the following info:
    # Name: Lipitor
    # Strength: 20 mg
    # Imprint: PD 156 20
    # Color: White
    # Shape: Oval

    assert any([True for i in extracted_info if i['pillName'] == 'Lipitor' and
                i['strength'] == '20 mg' and i['imprint'] == 'PD 156 20' and i['color'] == 'White' and i['shape'] == 'Oval'])

    # Assert there is a pill with the following info:

    # Name: Atorvastatin Calcium
    # Strength: 20 mg
    # Imprint: PD 156 20
    # Color: White
    # Shape: Oval

    assert any([True for i in extracted_info if i['pillName'] == 'Atorvastatin Calcium' and
                i['strength'] == '20 mg' and i['imprint'] == 'PD 156 20' and i['color'] == 'White' and i['shape'] == 'Oval'])

    # Assert there is a pill with the following info:

    # Name: Duloxetine Hydrochloride Delayed-Release
    # Strength: 20 mg
    # Imprint: A156 20
    # Color: Beige / Orange
    # Shape: Capsule/Oblong

    assert any([True for i in extracted_info if i['pillName'] == 'Duloxetine Hydrochloride Delayed-Release' and
                i['strength'] == '20 mg' and i['imprint'] == 'A156 20' and i['color'] == 'Beige / Orange' and i['shape'] == 'Capsule/Oblong'])

    # Assert there is a pill with the following info:

    # Name: Lipitor
    # Strength: 20 mg
    # Imprint: VLE 156 20
    # Color: White
    # Shape: Oval

    assert any([True for i in extracted_info if i['pillName'] == 'Lipitor' and
                i['strength'] == '20 mg' and i['imprint'] == 'VLE 156 20' and i['color'] == 'White' and i['shape'] == 'Oval'])


