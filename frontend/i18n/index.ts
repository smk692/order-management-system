import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import koCommon from './locales/ko/common.json';
import koStatus from './locales/ko/status.json';
import enCommon from './locales/en/common.json';
import enStatus from './locales/en/status.json';

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      ko: { common: koCommon, status: koStatus },
      en: { common: enCommon, status: enStatus },
    },
    fallbackLng: 'ko',
    defaultNS: 'common',
    interpolation: { escapeValue: false },
  });

export default i18n;
