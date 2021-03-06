package nivel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import character.PC;
import entidades.Entidad;
import entidades.Jugador;
import entidades.JugadorMP;
import gfx.Pantalla;
import tiles.Tile;

public class Nivel {

	// array de ids
	private byte[] tiles;
	//matriz de obstaculos
	private boolean [][] obstaculos;
	private int ancho;
	private int alto;
	private List<Entidad> entidades = new ArrayList<Entidad>();
	private List<PC> listaPj = new ArrayList<PC>();
	
	
	private String imgPath;
	private BufferedImage imagen;
	
	
	
	public Nivel(String imgPath) {
		
		if(imgPath != null)
		{
			//se levanta nivel desde archivo
			this.imgPath = imgPath;
			this.cargarNivelDesdeArchivo();
			
		
		}
		else
		{
			//se genera un nivel por codigo
			this.ancho = 64;
			this.alto = 64;
			tiles = new byte[ancho * alto];
			obstaculos = new boolean[ancho][alto];
			this.generarNivel();  
		}
		
		
	}
	
	private void cargarNivelDesdeArchivo()
	{
		try
		{
			this.imagen = ImageIO.read(Nivel.class.getResource(this.imgPath));
			this.ancho = imagen.getWidth();
			this.alto = imagen.getHeight();
			tiles = new byte [ancho*alto];
			obstaculos = new boolean[ancho][alto];
			this.cargarTiles();
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void cargarTiles()
	{
		//carga los tiles desde la imagen
		
		//parseamos el color de la imagen en un valor id
		
		int [] coloresTiles = this.imagen.getRGB(0, 0, ancho, alto, null, 0, ancho);
		for(int y = 0; y < alto; y++)
		{
			for(int x = 0; x < ancho; x++)
			{
				tileCheck: for (Tile t : Tile.tiles) {
					if(t != null && t.getNivelColor() == coloresTiles[x + y * ancho])
					{
						this.tiles[x + y * ancho] = t.getId();
						this.obstaculos[x][y] = t.getObs();
						break tileCheck; //corta cuando encuentre un nulo
					}
				}
			}
		}
		
	}
	
	@SuppressWarnings("unused")
	private void guardarNivelEnArchivo()
	{
		try
		{
			ImageIO.write(imagen, "png", new File(Nivel.class.getResource(this.imgPath).getFile()) );
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void alterTile(int x, int y, Tile nuevoTile)
	{
		this.tiles[x+y*ancho] = nuevoTile.getId();
		imagen.setRGB(x, y, nuevoTile.getNivelColor());
		
	}

	public void generarNivel() {
		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				
				tiles[x + y * ancho] = Tile.PASTO.getId();
				if(x * y % 10 < 7)
				{
					tiles[x + y * ancho] = Tile.PASTO.getId();
				}
				else
				{
					tiles[x + y * ancho] = Tile.EDIFICIO.getId();
				}
				
			}
		}
	}
	public void actualizar()
	{
		for (Entidad e : entidades) {
			e.actualizar();
		}
	}
	public void renderTiles(Pantalla pantalla, int xOffset, int yOffset) {
		
		if (xOffset < 0) 
			xOffset = 0;

		if (xOffset > ((ancho << 3) - pantalla.getAncho()))
			xOffset = ((ancho << 3) - pantalla.getAncho());
		
		if (yOffset < 0)
			yOffset = 0;
		
		if (yOffset > ((alto << 3) - pantalla.getAlto()))
			yOffset = ((alto << 3) - pantalla.getAlto());
		
		pantalla.setOffset(xOffset, yOffset);
		
		//yOffset >> 3 representa el ofset desde donde comienza la esquina sup izquierda de lo q se ve en pantalla
		for (int y = (yOffset >> 3); y < (yOffset + pantalla.getAlto() >> 3) + 1 ; y++)
		{
			for (int x = (xOffset >> 3); x < (xOffset + pantalla.getAncho() >> 3) + 1 ; x++) {
				getTile(x, y).render(pantalla, this, x << 3, y << 3);
			}
		}

		

	}
	public void renderizarEntidades(Pantalla pantalla)
	{
		for (Entidad e : entidades) {
			e.renderizar(pantalla);
		}
	}
	public Tile getTile(int x, int y)
	{
		if(x < 0 || x >= ancho || y < 0 || y >= alto)
			return Tile.VOID;
		return Tile.tiles[tiles[x + y * ancho]];
	}
	
	public int getAncho()
	{
		return ancho;
	}
	public int getAlto()
	{
		return alto;
	}
	
	public void addEntidad(Jugador entidad)
	{
		this.entidades.add(entidad);
	}
	public void addPj(PC personaje)
	{
		this.listaPj.add(personaje);
	}
	
	public boolean [][] getObstaculos()
	{
		return this.obstaculos;
	}
	public boolean getObstaculos(int i, int j)
	{
		return this.obstaculos[i][j];
	}
	public void recibirListaNueva(ArrayList<PC> listaNueva)
	{
		this.listaPj = listaNueva;
		actualizarListaEntidades();
	}
	public void actualizarListaEntidades()
	{
		boolean aux = false;
		for(int i = 0; i < listaPj.size(); i++)
		{
			for(int j = 0; j < entidades.size(); j++)
			{
				if(listaPj.get(i).getName().equals(entidades.get(j).getName())) {
					aux = true;
					entidades.get(j).setXY(listaPj.get(i).getX(),listaPj.get(i).getY() );
				}
			}
			
			if(!aux){
				entidades.add(new JugadorMP(listaPj.get(i), this));
			}
			aux = false;
		}	
	}
	
	
	
}
