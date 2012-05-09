import urllib2, sys, re, time

class UrlCache:
        '''makes testing a lot faster since you dont
        have to redownload stuff youve seen already'''
        def __init__(self, limit=50000):
                self.cache_list = []
                self.cache_dict = dict()
                self.limit = limit
        def get(self, url):
                if url in self.cache_dict:
                        #print 'got'
                        return self.cache_dict[url]
                url+='&maxlag=4'
                if url in self.cache_dict:
                        #print 'got'
                        return self.cache_dict[url]

                
        def add(self, url, txt):
                if url in self.cache_dict:
                        return None
                
                self.cache_dict[url] = txt
                self.cache_list.append(url)
                #print 'saved', url
                
                if len(self.cache_list) == self.limit:
                        #print 'rm'
                        self.cache_dict.pop(self.cache_list.pop(0))

UCH = UrlCache()
#print 'UrlCache loaded'

def getURL(url, retry=3, WIKI=True):
    #print 'getting', url
    
    res = UCH.get(url)
    if res != None:
            return res
    
    #testurl = 'http://en.wikipedia.org/w/index.php?title=Main_Page&action=view&maxlag=-1'
    if WIKI and 'maxlag' not in url:
        url = url + '&maxlag=4'
        
    req = urllib2.Request(url, headers={'User-Agent' : 'BritannicaProjBot contact:gabrovski@cs.dartmouth.edu'}) 
    try:
        con = urllib2.urlopen( req )
        txt = con.read()
        con.close()
        
        UCH.add(url, txt)
        return txt

    except urllib2.HTTPError,e:
        txt = e.fp.read()
        
        throttle = re.search('Waiting for .+?: (\d*?) seconds lagged', txt)
        if throttle != None:
            throttle = int(throttle.group(1))
            if throttle < 5: throttle = 5
            print 'throttled for', throttle

            time.sleep(throttle)
            return getURL(url)
        else:
            print sys.exc_info()[0], 'at', url
            if retry > 0:
                time.sleep(5)
                print 'retrying'
                return getURL(url, retry-1)
            else:
                print 'out of retries, keep going'
                UCH.add(url, '')
                return ''

    except KeyboardInterrupt:
            print 'Ctrl+C again to quit'
            time.sleep(5)
            return ''
    except:
        print 'some other error at getURL'
        print sys.exc_info()[0], 'at', url
        UCH.add(url, '')
        return ''
