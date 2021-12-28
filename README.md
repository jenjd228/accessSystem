# accessSystem
Access control system based on csv, xml and h2 data sources.

sudo systemctl start mongod.service - запуск монги

java -jar <название_файла> -env <путь_до_environment_properties> -log <путь_до_log4j2_xml> -help - вызов вспомогательной информации о программе.

Если не указывать -env и -log то будут использованы настройки по умолчанию. В лог будет выводиться только info информация о происходящих процессках.

Options

-db <barrier_id> -                                             Удаление барьера
   
-ds <subject_id> -                                             Удаление пользователя и доступов связаных с ним
   
-dsa <subject_id barrier_id> -                                 Удаление доступа пользователя
   
-dType <data_type> -                                           Указание типа данных(XML,H2,CSV) (по умолчанию h2)
  
-env <file_path> -                                             Путь до файла environment.properties
  
-ga <subject_id barrier_id year month day hours> -             Предоставление доступа к барьеру
   
-gac <subject_id barrier_id move_type(IN,OUT)> -               Вход или выход через барьер
  
-help -                                                        Информация по использованию сервиса
   
-log <file_path> -                                             Путь до файла log4j2.xml
   
-na <name_ color_> -                                           Создание нового пользователя (животное)
   
-nb <floor_> -                                                 Регистрация барьера
   
-nh <right_ name surname patronymic login password email> -    Создание нового пользователя (человек)
   
-nt <number_ color_> -                                         Создание нового пользователя (транспорт)
   
-pb -                                                          Вывод информации о всех барьерах
   
-ph <subject_id> -                                             Вывод истории пользователя
   
-ps -                                                          Вывод информации о всех юзерах
   
-psa <subject_id> -                                            Вывод информации о правах данного пользователя

-- HELP --
