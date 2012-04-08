from django.db import models

class Category(models.Model):
    name  = models.CharField(max_length=512, primary_key=True)

class Other(models.Model):
    name  = models.CharField(max_length=512, primary_key=True)

class Article(models.Model):
    name  = models.CharField(max_length=512, primary_key=True)
    wid   = models.BigIntegerField()
    birth = models.IntegerField(blank=True, null=True)
    death = models.IntegerField(blank=True, null=True)
    image = models.CharField(max_length=512)
    
    inf_on = models.ForeignKey('self', related_name='influences',blank=True, null=True)
    inf_by = models.ForeignKey('self', related_name='influenced', blank=True, null=True)
    peers      = models.ManyToManyField('self')

    categories = models.ManyToManyField(Category)
    
    other_links = models.ManyToManyField(Other)

    
    def __unicode__(self):
        return self.name

