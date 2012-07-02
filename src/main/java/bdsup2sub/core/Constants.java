/*
 * Copyright 2012 Miklos Juhasz (mjuhasz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bdsup2sub.core;

import bdsup2sub.bitmap.Palette;

public class Constants {

    public static final String APP_NAME = ApplicationAttributes.getInstance().getName();
    public static final String APP_VERSION = ApplicationAttributes.getInstance().getVersion();
    public static final String BUILD_DATE = ApplicationAttributes.getInstance().getBuildDate();
    public static final String DEVELOPERS = "Volker Oth, Miklos Juhasz";

    /** RED components of default DVD palette */
    public static final byte DEFAULT_PALETTE_RED[] = {
        (byte)0x00, (byte)0xf0, (byte)0xcc, (byte)0x99,
        (byte)0x33, (byte)0x11, (byte)0xfa, (byte)0xbb,
        (byte)0x33, (byte)0x11, (byte)0xfa, (byte)0xbb,
        (byte)0xfa, (byte)0xbb, (byte)0x33, (byte)0x11
    };

    /** GREEN components of default DVD palette */
    public static final byte DEFAULT_PALETTE_GREEN[] = {
        (byte)0x00, (byte)0xf0, (byte)0xcc, (byte)0x99,
        (byte)0x33, (byte)0x11, (byte)0x33, (byte)0x11,
        (byte)0xfa, (byte)0xbb, (byte)0xfa, (byte)0xbb,
        (byte)0x33, (byte)0x11, (byte)0xfa, (byte)0xbb
    };

    /** BLUE components of default DVD palette */
    public static final byte DEFAULT_PALETTE_BLUE[] = {
        (byte)0x00, (byte)0xf0, (byte)0xcc, (byte)0x99,
        (byte)0xfa, (byte)0xbb, (byte)0x33, (byte)0x11,
        (byte)0x33, (byte)0x11, (byte)0x33, (byte)0x11,
        (byte)0xfa, (byte)0xbb, (byte)0xfa, (byte)0xbb,
    };

    /** ALPHA components of default DVD palette */
    public static final byte DEFAULT_PALETTE_ALPHA[] = {
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
    };

    public static final Palette DEFAULT_DVD_PALETTE = new Palette(
            DEFAULT_PALETTE_RED,
            DEFAULT_PALETTE_GREEN,
            DEFAULT_PALETTE_BLUE,
            DEFAULT_PALETTE_ALPHA,
            true
    );

    public static final String[][] LANGUAGES = {
        {"English",      "en", "eng"},
        {"Abkhazian",    "ab", "abk"},
        {"Afar",         "aa", "aar"},
        {"Afrikaans",    "af", "afr"},
        {"Albanian",     "sq", "sqi"},
        {"Amharic",      "am", "amh"},
        {"Arabic",       "ar", "ara"},
        {"Aragonese",    "an", "arg"},
        {"Armenian",     "hy", "hye"},
        {"Assamese",     "as", "asm"},
        {"Avaric",       "av", "ava"},
        {"Avestan",      "ae", "ave"},
        {"Aymara",       "ay", "aym"},
        {"Azerbaijani",  "az", "aze"},
        {"Bambara",      "bm", "bam"},
        {"Bashkir",      "ba", "bak"},
        {"Basque",       "eu", "eus"},
        {"Belarusian",   "be", "bel"},
        {"Bengali",      "bn", "ben"},
        {"Bihari",       "bh", "bih"},
        {"Bislama",      "bi", "bis"},
        {"Bosnian",      "bs", "bos"},
        {"Breton",       "br", "bre"},
        {"Bulgarian",    "bg", "bul"},
        {"Burmese",      "my", "mya"},
        {"Cambodian",    "km", "khm"},
        {"Catalan",      "ca", "cat"},
        {"Chamorro",     "ch", "cha"},
        {"Chechen",      "ce", "che"},
        {"Chichewa",     "ny", "nya"},
        {"Chinese",      "zh", "zho"},
        {"Chuvash",      "cv", "chv"},
        {"Cornish",      "kw", "cor"},
        {"Corsican",     "co", "cos"},
        {"Cree",         "cr", "cre"},
        {"Croatian",     "hr", "hrv"},
        {"Czech",        "cs", "ces"},
        {"Danish",       "da", "dan"},
        {"Divehi",       "dv", "div"},
        {"Dzongkha",     "dz", "dzo"},
        {"Dutch",        "nl", "nld"},
        {"Esperanto",    "eo", "epo"},
        {"Estonian",     "et" ,"est"},
        {"Ewe",          "ee", "ewe"},
        {"Faroese",      "fo", "fao"},
        {"Fiji",         "fj", "fij"},
        {"Finnish",      "fi", "fin"},
        {"French",       "fr", "fra"},
        {"Frisian",      "fy", "fry"},
        {"Fulah",        "ff", "ful"},
        {"Gaelic",       "gd", "gla"},
        {"Galician",     "gl", "glg"},
        {"Ganda",        "lg", "lug"},
        {"Georgian",     "ka", "kat"},
        {"German",       "de", "deu"},
        {"Greek",        "el", "ell"},
        {"Greenlandic",  "kl", "kal"},
        {"Guarani",      "gn", "grn"},
        {"Gujarati",     "gu", "guj"},
        {"Haitian",      "ht", "hat"},
        {"Hausa",        "ha", "hau"},
        {"Hebrew",       "he", "heb"},
        {"Herero",       "hz", "her"},
        {"Hindi",        "hi", "hin"},
        {"Hiri Motu",    "ho", "hmo"},
        {"Hungarian",    "hu", "hun"},
        {"Icelandic",    "is", "isl"},
        {"Ido",          "io", "ido"},
        {"Igbo",         "ig", "ibo"},
        {"Indonesian",   "id", "ind"},
        {"Interlingua",  "ia", "ina"},
        {"Interlingue",  "ie", "ile"},
        {"Inupiaq",      "ik", "ipk"},
        {"Inuktitut",    "iu", "iku"},
        {"Irish",        "ga", "gle"},
        {"Italian",      "it", "ita"},
        {"Japanese",     "ja", "jpn"},
        {"Javanese",     "jv", "jav"},
        {"Kannada",      "kn", "kan"},
        {"Kanuri",       "kr", "kau"},
        {"Kashmiri",     "ks", "kas"},
        {"Kazakh",       "kk", "kaz"},
        {"Kikuyu",       "ki", "kik"},
        {"Kinyarwanda",  "rw", "kin"},
        {"Kirghiz",      "ky", "kir"},
        {"Komi",         "kv", "kom"},
        {"Kongo",        "kg", "kon"},
        {"Korean",       "ko", "kor"},
        {"Kuanyama",     "kj", "kua"},
        {"Kurdish",      "ku", "kur"},
        {"Lao",          "lo", "lao"},
        {"Latin",        "la", "lat"},
        {"Latvian",      "lv", "lav"},
        {"Limburgan",    "li", "lim"},
        {"Lingala",      "ln", "lin"},
        {"Lithuanian",   "lt", "lit"},
        {"Luba",         "lu", "lub"},
        {"Luxembourgish","lb","ltz"},
        {"Macedonian",   "mk", "mkd"},
        {"Malagasy",     "mg", "mlg"},
        {"Malay",        "ms", "msa"},
        {"Malayalam",    "ml", "mal"},
        {"Maltese",      "mt", "mlt"},
        {"Marshallese",  "mh", "mah"},
        {"Manx",         "gv", "glv"},
        {"Maori",        "mi", "mri"},
        {"Marathi",      "mr", "mar"},
        {"Mongolian",    "mn", "mon"},
        {"Nauru",        "na", "nau"},
        {"Navajo",       "nv", "nav"},
        {"Ndebele",      "nd", "nde"},
        {"Ndonga",       "ng", "ndo"},
        {"Nepali",       "ne", "nep"},
        {"Norwegian",    "no", "nor"},
        {"Occitan",      "oc", "oci"},
        {"Ojibwa",       "oj", "oji"},
        {"Oriya",        "or", "ori"},
        {"Oromo",        "om", "orm"},
        {"Ossetian",     "os", "oss"},
        {"Pali",         "pi", "pli"},
        {"Panjabi",      "pa", "pan"},
        {"Pashto",       "ps", "pus"},
        {"Persian",      "fa", "fas"},
        {"Polish",       "pl", "pol"},
        {"Portuguese",   "pt", "por"},
        {"Quechua",      "qu", "que"},
        {"Romansh",      "rm", "roh"},
        {"Romanian",     "ro", "ron"},
        {"Rundi",        "rn", "run"},
        {"Russian",      "ru", "rus"},
        {"Sami",         "se", "sme"},
        {"Samoan",       "sm", "smo"},
        {"Sango",        "sg", "sag"},
        {"Sanskrit",     "sa", "san"},
        {"Sardinian",    "sc", "srd"},
        {"Serbian",      "sr", "srp"},
        {"Shona",        "sn", "sna"},
        {"Sichuan Yi",   "ii", "iii"},
        {"Sindhi",       "sd", "snd"},
        {"Sinhalese",    "si", "sin"},
        {"Slavonic",     "cu", "chu"},
        {"Slovak",       "sk", "slk"},
        {"Slovenian",    "sl", "slv"},
        {"Somali",       "so", "som"},
        {"Sotho",        "st", "sot"},
        {"Spanish",      "es", "spa"},
        {"Sundanese",    "su", "sun"},
        {"Swahili",      "sw", "swa"},
        {"Swati",        "ss", "ssw"},
        {"Swedish",      "sv", "swe"},
        {"Tagalog",      "tl", "tgl"},
        {"Tahitian",     "ty", "tah"},
        {"Tajik",        "tg", "tgk"},
        {"Tamil",        "ta", "tam"},
        {"Tatar",        "tt", "tar"},
        {"Telugu",       "te", "tel"},
        {"Thai",         "th", "tha"},
        {"Tibetan",      "bo", "bod"},
        {"Tigrinya",     "ti", "tir"},
        {"Tonga",        "to", "ton"},
        {"Tsonga",       "ts", "tso"},
        {"Tswana",       "tn", "tsn"},
        {"Turkish",      "tr", "tur"},
        {"Turkmen",      "tk", "tuk"},
        {"Twi",          "tw", "twi"},
        {"Uighur",       "ug", "uig"},
        {"Ukrainian",    "uk", "ukr"},
        {"Urdu",         "ur", "urd"},
        {"Uzbek",        "uz", "uzb"},
        {"Venda",        "ve", "ven"},
        {"Vietnamese",   "vi", "vie"},
        {"Volapük",      "vo", "vol"},
        {"Welsh",        "cy", "cym"},
        {"Walloon",      "wa", "wln"},
        {"Wolof",        "wo", "wol"},
        {"Xhosa",        "xh", "xho"},
        {"Yiddish",      "yi", "yid"},
        {"Yoruba",       "yo", "yor"},
        {"Zhuang",       "za", "zha"},
        {"Zulu",         "zu", "zul"},
    };
}
