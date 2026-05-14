import requests
from bs4 import BeautifulSoup
import json
import re
import time

BATCH_SIZE = 500

CATEGORY_MAP = {
    'anime': 'ANIME_MANGA',
    'manga': 'ANIME_MANGA',
    'book': 'BOOKS',
    'novel': 'BOOKS',
    'literature': 'BOOKS',
    'cartoon': 'CARTOONS',
    'animated': 'CARTOONS',
    'film': 'FILMS',
    'movie': 'FILMS',
    'cinema': 'FILMS',
    'tv series': 'TV_SERIES',
    'television': 'TV_SERIES',
    'tv show': 'TV_SERIES',
    'game': 'GAMES',
    'video game': 'GAMES',
    'tabletop game': 'TABLETOP_GAMES',
    'board game': 'TABLETOP_GAMES',
    'music': 'MUSIC',
    'band': 'MUSIC',
    'theatre': 'THEATER_MUSICALS',
    'musical': 'THEATER_MUSICALS',
    'podcast': 'PODCASTS',
    'comic': 'COMICS',
    'comic book': 'COMICS',
    'celebrity': 'CELEBRITIES',
    'sport': 'SPORTS',
    'history': 'HISTORY',
    'mythology': 'MYTHOLOGY',
}

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
}

def map_category(cat_text):
    cat_lower = cat_text.lower().strip()
    for key, value in CATEGORY_MAP.items():
        if key in cat_lower:
            return value
    return 'OTHER'

def parse_wikipedia_list(url, category_fallback='OTHER', selector='ul li a', name_from='text'):
    try:
        resp = requests.get(url, headers=HEADERS, timeout=10)
        resp.raise_for_status()
    except:
        return []
    soup = BeautifulSoup(resp.text, 'html.parser')
    items = soup.select(selector)
    names = set()
    for item in items:
        if name_from == 'text':
            name = item.get_text(strip=True)
        elif name_from == 'href':
            href = item.get('href', '')
            name = href.split('/')[-1].replace('_', ' ')
        else:
            continue
        if name and len(name) > 1 and not re.search(r'\d', name):
            names.add(name)
    return [{"name": n, "category": category_fallback} for n in names]

def parse_wikipedia_table(url, name_col_index=0, category_col_index=None):
    try:
        resp = requests.get(url, headers=HEADERS, timeout=10)
        resp.raise_for_status()
    except:
        return []
    soup = BeautifulSoup(resp.text, 'html.parser')
    table = soup.find('table', class_='wikitable')
    if not table:
        return []
    rows = table.find_all('tr')
    fandoms = []
    for row in rows[1:]:
        cols = row.find_all('td')
        if len(cols) <= name_col_index:
            continue
        name = cols[name_col_index].get_text(strip=True)
        if not name or name == '—':
            continue
        if category_col_index is not None and len(cols) > category_col_index:
            cat_text = cols[category_col_index].get_text(strip=True)
            category = map_category(cat_text)
        else:
            category = 'OTHER'
        fandoms.append({"name": name, "category": category})
    return fandoms

def parse_wikipedia_category(url, category_type):
    """
    Парсит страницу категории Википедии для извлечения всех вложенных страниц.
    """
    print(f"  Обработка категории {category_type}: {url}")
    try:
        resp = requests.get(url, headers=HEADERS, timeout=10)
        resp.raise_for_status()
    except Exception as e:
        print(f"    Ошибка загрузки категории: {e}")
        return []

    soup = BeautifulSoup(resp.text, 'html.parser')
    # Все элементы списка в div-контейнере с id 'mw-pages'
    items = soup.select('div#mw-pages li a')
    names = []
    for a in items:
        name = a.get_text(strip=True)
        # Простая фильтрация: убираем слишком короткие и служебные названия
        if name and len(name) > 2 and not name.startswith(('Список', 'Категория', 'Википедия')):
            names.append({"name": name, "category": category_type})
    return names

def gather_all_fandoms():
    print("Сбор фандомов из русской Википедии...")
    all_fandoms = []

    # Список источников: (URL_категории, название_категории)
    sources = [
        # -------- ANIME_MANGA ----------
        ("https://ru.wikipedia.org/wiki/Категория:Списки_серий_аниме", "ANIME_MANGA"),
        ("https://ru.wikipedia.org/wiki/Категория:Аниме-фильмы", "ANIME_MANGA"),
        ("https://ru.wikipedia.org/wiki/Категория:Манга", "ANIME_MANGA"),

        # -------- BOOKS ----------
        ("https://ru.wikipedia.org/wiki/Категория:Книги-бестселлеры", "BOOKS"),
        ("https://ru.wikipedia.org/wiki/Категория:Романы-бестселлеры", "BOOKS"),
        ("https://ru.wikipedia.org/wiki/Категория:Литературные_премии", "BOOKS"),

        # -------- CARTOONS ----------
        ("https://ru.wikipedia.org/wiki/Категория:Мультсериалы_по_алфавиту", "CARTOONS"),
        ("https://ru.wikipedia.org/wiki/Категория:Полнометражные_мультфильмы", "CARTOONS"),
        ("https://ru.wikipedia.org/wiki/Категория:Анимационные_фильмы", "CARTOONS"),

        # -------- FILMS ----------
        ("https://ru.wikipedia.org/wiki/Категория:Кинофраншизы", "FILMS"),
        ("https://ru.wikipedia.org/wiki/Категория:Фильмы_по_алфавиту", "FILMS"),
        ("https://ru.wikipedia.org/wiki/Категория:Самые_кассовые_фильмы", "FILMS"),

        # -------- TV_SERIES ----------
        ("https://ru.wikipedia.org/wiki/Категория:Телесериалы_по_алфавиту", "TV_SERIES"),
        ("https://ru.wikipedia.org/wiki/Категория:Сериалы,_основанные_на_фильмах", "TV_SERIES"),

        # -------- GAMES ----------
        ("https://ru.wikipedia.org/wiki/Категория:Видеоигры_по_алфавиту", "GAMES"),
        ("https://ru.wikipedia.org/wiki/Категория:Франшизы_видеоигр", "GAMES"),

        # -------- TABLETOP_GAMES ----------
        ("https://ru.wikipedia.org/wiki/Категория:Настольные_игры", "TABLETOP_GAMES"),

        # -------- MUSIC ----------
        ("https://ru.wikipedia.org/wiki/Категория:Музыкальные_группы_по_алфавиту", "MUSIC"),
        ("https://ru.wikipedia.org/wiki/Категория:Музыкальные_альбомы_по_алфавиту", "MUSIC"),
        ("https://ru.wikipedia.org/wiki/Категория:Музыканты_по_алфавиту", "MUSIC"),

        # -------- THEATER_MUSICALS ----------
        ("https://ru.wikipedia.org/wiki/Категория:Мюзиклы_по_алфавиту", "THEATER_MUSICALS"),

        # -------- COMICS ----------
        ("https://ru.wikipedia.org/wiki/Категория:Супергерои_Marvel_Comics", "COMICS"),
        ("https://ru.wikipedia.org/wiki/Категория:Комиксы_по_алфавиту", "COMICS"),

        # -------- CELEBRITIES ----------
        ("https://ru.wikipedia.org/wiki/Категория:Знаменитости", "CELEBRITIES"),

        # -------- SPORTS ----------
        ("https://ru.wikipedia.org/wiki/Категория:Спортивные_лиги", "SPORTS"),
        ("https://ru.wikipedia.org/wiki/Категория:Спортивные_клубы", "SPORTS"),

        # -------- HISTORY ----------
        ("https://ru.wikipedia.org/wiki/Категория:Исторические_личности", "HISTORY"),
        ("https://ru.wikipedia.org/wiki/Категория:Монархи", "HISTORY"),

        # -------- MYTHOLOGY ----------
        ("https://ru.wikipedia.org/wiki/Категория:Мифические_существа", "MYTHOLOGY"),

        # -------- PODCASTS ----------
        ("https://ru.wikipedia.org/wiki/Категория:Подкасты_по_алфавиту", "PODCASTS"),
    ]

    total_added = 0
    for url, category in sources:
        items = parse_wikipedia_category(url, category)
        all_fandoms.extend(items)
        total_added += len(items)
        print(f"    Добавлено {len(items)}")
        time.sleep(1)  # Задержка для соблюдения этики парсинга

    # ... (остальной код: удаление дубликатов и сохранение в файлы)

def save_batches(fandoms, batch_size=BATCH_SIZE):
    total = len(fandoms)
    print(f'Сохранение {total} фандомов порциями по {batch_size}...')
    for i in range(0, total, batch_size):
        batch = fandoms[i:i+batch_size]
        filename = f'fandom_batch_{i//batch_size + 1}.json'
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump({"fandoms": batch}, f, ensure_ascii=False, indent=2)
        print(f'  Сохранён {filename} (записи {i+1}–{min(i+batch_size, total)})')

if __name__ == '__main__':
    try:
        fandoms = gather_all_fandoms()
        if fandoms:
            save_batches(fandoms)
        else:
            print("Не удалось найти ни одного фандома.")
    except Exception as e:
        print(f'Критическая ошибка: {e}')