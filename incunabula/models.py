from django.db import models

class MasterArticle(models.Model):
    name = models.CharField(max_length=256, primary_key=True)    

class Article(models.Model):
    name = models.CharField(max_length=256)
    art_id = models.IntegerField()
    art_ed = models.IntegerField()
    text = models.TextField()
    
    prank = models.FloatField()
    volume_score = models.FloatField()

    match_master = models.ForeignKey(MasterArticle)
    match_score = models.FloatField()

    def __unicode__(self):
        return self.name
    
class Reference(models.Model):
    article = models.ForeignKey(Article)
    ref_ed = models.IntegerField()
    ref_id = models.IntegerField()
    
    def __unicode__(self):
        ref = Article.objects.get(art_id=self.ref_id, art_ed=ref_ed)
        return ref.name



