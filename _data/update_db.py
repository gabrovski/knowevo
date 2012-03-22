import sqlite3, cPickle, re, time, psycopg2, os

def get_conn():
    c = psycopg2.connect('dbname=knowevo user=knowevo host=localhost password=12345c7890')
    return c

def clean_db():
    conn = get_conn()
    c = conn.cursor()
    c.execute('DELETE FROM incunabula_article')
    c.execute('DELETE FROM incunabula_match')
    c.execute('DELETE FROM incunabula_masterarticle')
    conn.commit()
    c.close()
    
    print 'cleaned db'

def insert_master_list(revw):
    conn = get_conn()
    c = conn.cursor()

    for k in revw.keys():
        c.execute('insert into incunabula_masterarticle\n'+
                  '(name) values(%s)', (k,))
    conn.commit()
    c.close()

    print 'inserted master articles'

def insert_matching_articles(revw):
    conn = get_conn()
    c = conn.cursor()

    for k in revw.keys():
        c.execute('select id from incunabula_masterarticle\n'+
                  'where name = %s', (k,))
        for row in c:
            aid, = row
        print aid
        for ed in revw[k]['editions'].keys():
            a = revw[k]['editions'][ed][0]

            c.execute('insert into incunabula_article\n'+
                      '(name,art_id,art_ed,text,prank,volume_score)\n'+
                      'values(%s,%s,%s,%s,%s,%s) returning id', 
                      (a['name'], a['id'], ed, a['txt'],0.0,0.0))

            for row in c:
                match_id, = row
            
            c.execute('insert into incunabula_match\n'+
                      '(article_id,match_ed,match_id,match_score)\n'+
                      'values(%s,%s,%s,%s)', (aid, ed, match_id, -1.0))
            
    conn.commit()
    c.close()
    
    print 'inserted articles and matches'


def load(path):
    f = open(path)
    revw = cPickle.load(f)
    f.close()
    return revw

def process_split():
    clean_db()
    
    for name in os.listdir('split'):
        revw = load('split/'+name)
        insert_master_list(revw)
        insert_matching_articles(revw)
    
    

if __name__ == '__main__':
    process_split()
                  
