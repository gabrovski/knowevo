from django.db import models

class Category(models.Model):
    name  = models.CharField(max_length=512, primary_key=True)

class Other(models.Model):
    name  = models.CharField(max_length=512, primary_key=True)

class Article(models.Model):
    name  = models.CharField(max_length=512, primary_key=True)
    wid   = models.BigIntegerField()
    birth = models.IntegerField()
    death = models.IntegerField()
    image = models.CharField(max_length=512)
    
    influences = models.ManyToManyField('self')
    influenced = models.ManyToManyField('self')
    peers      = models.ManyToManyField('self')

    categories = models.ManyToManyField(Category)
    
    other_links = models.ManyToManyField(Other)

