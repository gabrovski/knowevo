import sqlite3, cPickle, re, time, psycopg2

def get_conn():
    #c = sqlite3.connect('../db.sqlite')
    c = psycopg2.connect('dbname=knowevo user=knowevo host=localhost password=12345c7890')
    #c.text_factory = str
    return c

def format_name(name):
    name = re.sub('[\(\)]' ,'', name)
    name = re.sub('[ -]', '_', name)
    return name

def insert_articles(path, ed):
    f = open(path)
    arts = cPickle.load(f)
    f.close()

    conn = get_conn()
    c = conn.cursor()

    for a in arts:
        if 'id' not in a:
            print 'id not found'
            break
        
        name, art_id, art_ed, text = a['name'], a['id'], ed, a['txt']
        if ed == 15:
            text = ''
        print name

        c.execute('insert into incunabula_article\n'+
                  'values(null,%s,%d,%d,%s)', (name, art_id, art_ed, text))

    conn.commit()
    c.close()

def clean_db():
    conn = get_conn()
    c = conn.cursor()
    c.execute('DELETE FROM incunabula_article')
    c.execute('DELETE FROM incunabula_match')
    c.execute('DELETE FROM incunabula_masterarticle')
    conn.commit()
    c.close()

def insert_master_list(revw):
    conn = get_conn()
    c = conn.cursor()

    for k in revw.keys():
        c.execute('insert into incunabula_masterarticle\n'+
                  '(name) values(%s)', (k,))
    conn.commit()
    c.close()

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


def load(path):
    f = open(path)
    revw = cPickle.load(f)
    f.close()
    return revw

if __name__ == '__main__':
    clean_db()
    revw = load('sample_revw.pkl')
    insert_master_list(revw)
    insert_matching_articles(revw)
    
                  
