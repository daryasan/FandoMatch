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

def gather_all_fandoms():
    all_fandoms = []

    sources = [
        # -------- ANIME_MANGA ----------
        ("https://en.wikipedia.org/wiki/List_of_anime_series_by_episode_count", "ANIME_MANGA", "table", None, "Аниме по числу серий"),
        ("https://en.wikipedia.org/wiki/List_of_highest-grossing_anime_films", "ANIME_MANGA", "table", None, "Кассовые аниме-фильмы"),
        ("https://en.wikipedia.org/wiki/List_of_anime_franchises_by_episode_count", "ANIME_MANGA", "table", None, "Аниме-франшизы"),
        ("https://en.wikipedia.org/wiki/List_of_manga_series_by_volume_count", "ANIME_MANGA", "table", None, "Манга по числу томов"),
        ("https://en.wikipedia.org/wiki/List_of_best-selling_manga", "ANIME_MANGA", "table", None, "Лучшие манги"),

        # -------- BOOKS ----------
        ("https://en.wikipedia.org/wiki/List_of_best-selling_books", "BOOKS", "table", None, "Книги-бестселлеры"),
        ("https://en.wikipedia.org/wiki/List_of_best-selling_novels", "BOOKS", "table", None, "Романы-бестселлеры"),
        ("https://en.wikipedia.org/wiki/List_of_best-selling_fiction_authors", "BOOKS", "table", None, "Авторы-бестселлеры"),
        ("https://en.wikipedia.org/wiki/List_of_literary_awards", "BOOKS", "list", "ul li a", "Литературные премии"),
        ("https://en.wikipedia.org/wiki/List_of_children%27s_classic_books", "BOOKS", "list", "ul li i a", "Детские классические книги"),

        # -------- CARTOONS ----------
        ("https://en.wikipedia.org/wiki/List_of_animated_television_series", "CARTOONS", "table", None, "Мультсериалы"),
        ("https://en.wikipedia.org/wiki/List_of_animated_feature_films", "CARTOONS", "table", None, "Мультфильмы"),
        ("https://en.wikipedia.org/wiki/List_of_highest-grossing_animated_films", "CARTOONS", "table", None, "Кассовые мультфильмы"),

        # -------- FILMS ----------
        ("https://en.wikipedia.org/wiki/List_of_highest-grossing_films", "FILMS", "table", None, "Самые кассовые фильмы"),
        ("https://en.wikipedia.org/wiki/List_of_film_franchises", "FILMS", "table", None, "Кинофраншизы"),
        ("https://en.wikipedia.org/wiki/List_of_films_considered_the_best", "FILMS", "list", "ul li i a", "Лучшие фильмы"),
        ("https://en.wikipedia.org/wiki/List_of_films_with_most_Academy_Award_wins", "FILMS", "table", None, "Фильмы-оскароносцы"),

        # -------- TV_SERIES ----------
        ("https://en.wikipedia.org/wiki/List_of_most_watched_television_broadcasts", "TV_SERIES", "table", None, "Самые рейтинговые ТВ-шоу"),
        ("https://en.wikipedia.org/wiki/List_of_longest-running_television_series", "TV_SERIES", "table", None, "Долгоиграющие сериалы"),
        ("https://en.wikipedia.org/wiki/List_of_television_programmes_based_on_films", "TV_SERIES", "list", "ul li i a", "Сериалы по фильмам"),

        # -------- GAMES ----------
        ("https://en.wikipedia.org/wiki/List_of_best-selling_video_games", "GAMES", "table", None, "Самые продаваемые игры"),
        ("https://en.wikipedia.org/wiki/List_of_best-selling_video_game_franchises", "GAMES", "table", None, "Игровые франшизы"),
        ("https://en.wikipedia.org/wiki/List_of_video_game_franchises", "GAMES", "list", "ul li a", "Видеоигровые франшизы"),
        ("https://en.wikipedia.org/wiki/List_of_highest-grossing_media_franchises", "GAMES", "table", None, "Медиафраншизы"),

        # -------- TABLETOP_GAMES ----------
        ("https://en.wikipedia.org/wiki/List_of_board_games", "TABLETOP_GAMES", "list", "ul li i a", "Настольные игры"),
        ("https://en.wikipedia.org/wiki/List_of_tabletop_games", "TABLETOP_GAMES", "list", "ul li a", "Настолки общий список"),

        # -------- MUSIC ----------
        ("https://en.wikipedia.org/wiki/List_of_best-selling_music_artists", "MUSIC", "table", None, "Музыканты"),
        ("https://en.wikipedia.org/wiki/List_of_best-selling_albums", "MUSIC", "table", None, "Альбомы"),
        ("https://en.wikipedia.org/wiki/List_of_band_names", "MUSIC", "list", "ul li i a", "Названия групп"),
        ("https://en.wikipedia.org/wiki/List_of_musical_ensembles_by_genre", "MUSIC", "list", "ul li a", "Коллективы по жанрам"),

        # -------- THEATER_MUSICALS ----------
        ("https://en.wikipedia.org/wiki/List_of_musicals", "THEATER_MUSICALS", "list", "ul li i a", "Мюзиклы"),
        ("https://en.wikipedia.org/wiki/List_of_the_most_frequently_performed_musicals", "THEATER_MUSICALS", "table", None, "Популярные мюзиклы"),

        # -------- COMICS ----------
        ("https://en.wikipedia.org/wiki/List_of_comic_book_superheroes", "COMICS", "list", "ul li a", "Супергерои комиксов"),
        ("https://en.wikipedia.org/wiki/List_of_comic_books", "COMICS", "list", "ul li i a", "Комиксы"),
        ("https://en.wikipedia.org/wiki/List_of_comic_book_films", "COMICS", "table", None, "Фильмы по комиксам"),

        # -------- CELEBRITIES ----------
        ("https://en.wikipedia.org/wiki/List_of_most_followed_Instagram_accounts", "CELEBRITIES", "table", None, "Звёзды Instagram"),
        ("https://en.wikipedia.org/wiki/Forbes_Celebrity_100", "CELEBRITIES", "table", None, "Forbes знаменитости"),
        ("https://en.wikipedia.org/wiki/List_of_Academy_Award-winning_actors", "CELEBRITIES", "list", "ul li a", "Оскароносные актёры"),

        # -------- SPORTS ----------
        ("https://en.wikipedia.org/wiki/List_of_sports_leagues", "SPORTS", "list", "ul li a", "Спортивные лиги"),
        ("https://en.wikipedia.org/wiki/List_of_professional_sports_teams", "SPORTS", "list", "ul li a", "Профессиональные команды"),
        ("https://en.wikipedia.org/wiki/List_of_sports_clubs_nicknames", "SPORTS", "list", "ul li a", "Прозвища клубов"),

        # -------- HISTORY ----------
        ("https://en.wikipedia.org/wiki/List_of_historical_people_by_net_worth", "HISTORY", "table", None, "Исторические личности"),
        ("https://en.wikipedia.org/wiki/List_of_monarchs_by_nickname", "HISTORY", "list", "ul li a", "Монархи по прозвищам"),

        # -------- MYTHOLOGY ----------
        ("https://en.wikipedia.org/wiki/List_of_mythological_creatures", "MYTHOLOGY", "list", "ul li a", "Мифологические существа"),
        ("https://en.wikipedia.org/wiki/List_of_folklore_genres", "MYTHOLOGY", "list", "ul li a", "Фольклорные жанры"),

        # -------- PODCASTS ----------
        ("https://en.wikipedia.org/wiki/List_of_podcasts", "PODCASTS", "list", "ul li i a", "Подкасты"),
    ]

    total_added = 0
    for url, category, src_type, selector, desc in sources:
        print(f"  {desc} ({category})...")
        try:
            if src_type == 'table':
                items = parse_wikipedia_table(url, name_col_index=0, category_col_index=None)
                for item in items:
                    item['category'] = category
            else:
                items = parse_wikipedia_list(url, category_fallback=category, selector=selector, name_from='text')
            items = [item for item in items if len(item['name']) >= 2]
            all_fandoms.extend(items)
            total_added += len(items)
            print(f"Added{len(items)}")
        except Exception as e:
            print(f"Error: {e}")
        time.sleep(0.5)

    main_fandoms = parse_wikipedia_table('https://en.wikipedia.org/wiki/List_of_fandom_names', name_col_index=0, category_col_index=2)
    all_fandoms.extend(main_fandoms)
    print(f"Added {len(main_fandoms)}")

    seen = set()
    unique = []
    for f in all_fandoms:
        name_lower = f['name'].lower()
        if name_lower not in seen:
            seen.add(name_lower)
            unique.append(f)
    print(f"Unique: {len(unique)}")
    return unique

def save_batches(fandoms, batch_size=BATCH_SIZE):
    total = len(fandoms)
    print(f'Saving {total} by {batch_size}...')
    for i in range(0, total, batch_size):
        batch = fandoms[i:i+batch_size]
        filename = f'fandom_batch_{i//batch_size + 1}.json'
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump({"fandoms": batch}, f, ensure_ascii=False, indent=2)
        print(f'Saved {filename} (records {i+1}–{min(i+batch_size, total)})')

if __name__ == '__main__':
    try:
        fandoms = gather_all_fandoms()
        if fandoms:
            save_batches(fandoms)
        else:
            print("no fandoms found")
    except Exception as e:
        print(f'error: {e}')