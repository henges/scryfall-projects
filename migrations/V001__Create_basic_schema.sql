DROP SCHEMA IF EXISTS scryfall CASCADE;

CREATE SCHEMA scryfall;

CREATE TYPE scryfall.game AS ENUM (
    'ARENA',
    'MTGO',
    'PAPER'
    );

CREATE TYPE scryfall.colour AS ENUM (
    'W',
    'U',
    'B',
    'R',
    'G',
    'COLOURLESS'
    );

CREATE TYPE scryfall.format AS ENUM (
    'STANDARD',
    'PIONEER',
    'MODERN',
    'LEGACY',
    'VINTAGE',
    'COMMANDER',
    'ALCHEMY',
    'EXPLORER',
    'BRAWL',
    'HISTORIC',
    'PAUPER'
    );

CREATE TYPE scryfall.rarity AS ENUM (
    'S',
    'C',
    'U',
    'R',
    'M'
    );

CREATE TABLE scryfall.set
(
    code         varchar(8) primary key not null,
    name         text                   not null,
    release_date date                   not null
);

CREATE TABLE scryfall.card
(
    id              uuid primary key      not null,
    name            text                  not null,
    formats         scryfall.format array not null,
    colour_identity scryfall.colour array not null,
    keywords        text array            not null
);

CREATE TABLE scryfall.card_face
(
    card_id     uuid                  not null,
    name        text                  not null,
    mana_value  int                   not null,
    mana_cost   text array            not null, -- Should be array of mana symbols
    types       text array            not null,
    subtypes    text array            not null,
    oracle_text text                  not null,
    power       text                  null,
    toughness   text                  null,
    loyalty     text                  null,
    colours     scryfall.colour array not null,
    foreign key (card_id) references scryfall.card (id),
    primary key (card_id, name)
);

COMMENT
ON column scryfall.card.formats IS 'Not currently modelling "restricted"-ness';

CREATE TABLE scryfall.card_edition
(
    id               uuid                not null, -- Individual ID
    card_id          uuid                not null, -- Oracle ID
    set_code         varchar(8)          not null,
    collector_number text                not null,
    rarity           scryfall.rarity     not null,
    is_reprint       bool                not null default false,
    games            scryfall.game array not null,
    scryfall_url     text                not null,
    foreign key (card_id) references scryfall.card (id),
    foreign key (set_code) references scryfall.set (code),
    primary key (id, card_id)
);
