package fr.iutlens.trafficjam;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import fr.iutlens.trafficjam.traffic.Car;
import fr.iutlens.trafficjam.traffic.LevelMap;
import fr.iutlens.trafficjam.traffic.Track;
import fr.iutlens.trafficjam.traffic.Traffic;
import fr.iutlens.trafficjam.util.CoordSystem;
import fr.iutlens.trafficjam.util.SpriteSheet;

/**
 * Created by dubois on 20/01/15.
 */
public class TrafficView extends View {

    LevelMap map;
    CoordSystem coord;

    SpriteSheet sprite;
    Traffic traffic;

    // Entre 0 et 1, proportion du mouvent déjà effectué.
    private float progress;

    // Transformation permettant le centrage de la vue.
    private Matrix transform, reverse;

    // Rectangle réutilisable (pour éviter les instanciations)
    private RectF tmp;


    // Configuration du mode de dessin
    static PaintFlagsDrawFilter setfil= new PaintFlagsDrawFilter(0,
                Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private Rect src;


    // 3 constructeurs obligatoires pour une vue. Les 3 appellent init() pour ne pas dupliquer le code.

    public TrafficView(Context context) {
        super(context);
        init();
    }

    public TrafficView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrafficView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /***
     * Fait progresser la vue d'une image
     */
    void act(){
       progress += 0.1; // les voitures en mouvement avancent de 10% d'une case
        if (progress >=1){ // Si on arrive à la case d'après, on recalcule les prochains mouvements.
            progress = 0;
            traffic.moveAll();
        }
        this.invalidate(); // La vue a changé, on demande le rafraîchissement de l'affichage.
    }

    void init(){
        coord = new CoordSystem(-20,10,15,15);

        int[][] data = {
                {0,0,1,1,0,0},
                {0,0,1,1,0,0},
                {1,1,1,1,1,1},
                {1,1,1,1,1,1},
                {0,0,1,1,0,0},
                {0,0,1,1,0,0}};
        map = new LevelMap(data);

        Track[] track = new Track[]{
                new Track("-1:2:1111111"),
                new Track("3:-1:000000"),
                new Track("2:6:222333"),
                new Track("6:3:333000"),
        };
        traffic = new Traffic(map, track);

        transform = new Matrix();
        reverse = new Matrix();

        sprite = SpriteSheet.get(this.getContext(), R.drawable.sprite);
        src = new Rect(0,0, sprite.w, sprite.h);
    }

    public void onDraw(Canvas canvas){
        canvas.setDrawFilter(setfil);


        // on sauvegarde la transformation en cours et on applique la transformation désirée
        canvas.save();
        canvas.concat(transform);

        if (sprite == null){ //si les sprites ne sont pas chargé, on ne fait rien.
            return;
        }


    //On parcours la carte
        for(int i = 0; i < map.getWidth(); ++i){
            for(int j = 0; j < map.getHeight(); ++j){
                canvas.drawBitmap(sprite.getBitmap(map.map[i][j]), src,coord.bounds(i,j,i+1,j+1,tmp),null);
            }
        }

     // On parcours les voitures
        for(Car car : traffic.getCar()){
            float i,j;
            i = car.getX(progress);
            j = car.getY(progress);
            canvas.drawBitmap(sprite.getBitmap((car.getAngle() & 1)+2), src,coord.bounds(i,j,i+1,j+1,tmp),null);
        }


        // On restore la transformation originale.
        canvas.restore();
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        setZoom(w, h);
    }

    /***
     * Calcul du centrage du contenu de la vue
     * @param w
     * @param h
     */
    private void setZoom(int w, int h) {
        if (w<=0 ||h <=0) return;

        // Dimensions dans lesquelles ont souhaite dessiner
        RectF src = coord.bounds(0,0,map.getWidth(),map.getHeight(),tmp);

        // Dimensions à notre disposition
        RectF dst = new RectF(0,0,w,h);

        // Calcul de la transformation désirée (et de son inverse)
        transform.setRectToRect(src,dst, Matrix.ScaleToFit.CENTER);
        transform.invert(reverse);
    }


}
