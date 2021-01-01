package fr.dzzd.glsprite;


public interface GLViewListener 
{

    public void onInit();
    
    public void onResize(float width, float height, String units);
    
    public void onLoop();
    
}
